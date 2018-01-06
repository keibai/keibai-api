package main.java.socket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.AuctionDAO;
import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.UserDAO;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.User;
import main.java.models.meta.BodyWS;
import main.java.utils.HttpSession;
import main.java.utils.JsonCommon;
import main.java.utils.Logger;


import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BidWS implements WS {

    public static final String INVALID_AMOUNT_ERROR = "Invalid amount";
    public static final String AUCTION_ID_ERROR = "Missing auction ID.";
    public static final String SUBSCRIPTION_ERROR = "User not found inside the auction.";
    public static final String NO_CREDIT = "Not enough credit.";
    public static final String LOW_BID_STARTING_PRICE = "Bid cannot be lower than initial starting price";
    public static final String AUCTION_DOES_NOT_EXIST = "Auction does not exist";
    public static final String AUCTION_NOT_IN_PROGRESS = "Auction is not in progress";
    public static final String LOW_BID_HIGHER_BID = "You cannot bid lower than the highest bid.";

    public static final String TYPE_AUCTION_SUBSCRIBE = "AuctionSubscribe";
    public static final String TYPE_AUCTION_BID = "AuctionBid";

    private static final Map<Integer, Set<BidWS>> connected = new ConcurrentHashMap<>(); // Auction id <-> Sockets
    private int subscribed = -1; // Auction on which the current user is subscribed to.

    Session session;
    HttpSession httpSession;
    WSSender<BodyWS> sender = new BodyWSSender();

    @Override
    public void onOpen(Session session, HttpSession httpSession) {
        this.session = session;
        this.httpSession = httpSession;
    }

    @Override
    public void onMessage(Session session, BodyWS body) {
        switch(body.type) {
            case TYPE_AUCTION_SUBSCRIBE: {
                onAuctionSubscribe(body);
                break;
            }
            case TYPE_AUCTION_BID: {
                onAuctionBid(body);
                break;
            }
        }
    }

    protected void onAuctionSubscribe(BodyWS body) {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        Auction unsafeAuction = new Gson().fromJson(body.json, Auction.class);

        if (unsafeAuction.id == 0) {
            // jsonResponse.error(AUCTION_ID_ERROR);
            System.out.println("Empty auction ID");
            return;
        }

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(unsafeAuction.id);
        } catch (DAOException e) {
            Logger.error("Get auction by ID", String.valueOf(unsafeAuction.id), e.toString());
            // jsonResponse.internalServerError();
            return;
        }
        if (dbAuction == null) {
            // jsonResponse.error(AUCTION_NOT_EXIST_ERROR);
            System.out.println("Auction " + unsafeAuction.id + " does not exist.");
            return;
        }

        removeSubscription();
        subscribed = dbAuction.id;
        synchronized (connected) {
            if (!connected.containsKey(subscribed)) {
                connected.put(subscribed, ConcurrentHashMap.newKeySet());
            }
        }
        connected.get(subscribed).add(this);
        System.out.println(connected);
        System.out.println(dbAuction);

        BodyWS okBody = new BodyWS();
        okBody.status = 200;
        okBody.json = JsonCommon.ok();
        sender.reply(session, body, okBody);
    }

    protected void onAuctionBid(BodyWS body) {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        int userId = httpSession.userId();
        if (userId == -1) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        Bid unsafeBid;
        try {
            unsafeBid = new Gson().fromJson(body.json, Bid.class);
        } catch (JsonSyntaxException e) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeBid == null) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeBid.amount <= 0.0) {
            String json = JsonCommon.error(INVALID_AMOUNT_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (unsafeBid.auctionId == 0) {
            String json = JsonCommon.error(AUCTION_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // TODO test
        if (unsafeBid.auctionId != subscribed) {
            String json = JsonCommon.error(SUBSCRIPTION_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        User user;
        try {
            user = userDAO.getById(userId);
        } catch (DAOException e) {
            Logger.error("Get user by ID", String.valueOf(userId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getById(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID", String.valueOf(unsafeBid.auctionId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (auction == null) {
            String json = JsonCommon.error(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (!auction.status.equals(Auction.IN_PROGRESS)) {
            String json = JsonCommon.error(AUCTION_NOT_IN_PROGRESS);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        List<Bid> auctionBids;
        try {
            auctionBids = bidDAO.getListByAuctionId(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get bid list by auction ID", String.valueOf(unsafeBid.auctionId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        // User bid amount has to be higher or equal than auction starting price.
        if (unsafeBid.amount < auction.startingPrice) {
            String json = JsonCommon.error(LOW_BID_STARTING_PRICE);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // User has enough credit
        if (user.credit < unsafeBid.amount) {
            String json = JsonCommon.error(NO_CREDIT);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // User bid has to be higher than the maximum bid for that auction.
        if (auctionBids.size() > 0) {
            Bid maxBid = Collections.max(auctionBids);
            if ((unsafeBid.amount - maxBid.amount) <= 0) {
                String json = JsonCommon.error(LOW_BID_HIGHER_BID);
                sender.reply(session, body, BodyWSCommon.error(json));
                return;
            }
        }

        Bid newBid = new Bid();
        newBid.amount = unsafeBid.amount;
        newBid.auctionId = unsafeBid.auctionId;
        newBid.ownerId = this.httpSession.userId();

        Bid dbBid;
        try {
            dbBid = bidDAO.create(newBid);
        } catch (DAOException e) {
            Logger.error("Create bid", newBid.toString(), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }


        String json = new Gson().toJson(dbBid);
        sender.reply(session, body, BodyWSCommon.ok(json));

        // Broadcast bid to everyone in the auction.
        auctionBidded(dbBid);
    }

    protected void auctionBidded(Bid newBid) {
        System.out.println("returning");
        System.out.println(newBid);
        BodyWS body = new BodyWS();
        body.type = "AuctionBidded";
        body.json = new Gson().toJson(newBid);

        List<Session> sessions = connected.get(newBid.auctionId).parallelStream().map(b -> b.session).collect(Collectors.toList());
        sender.send(sessions, body);
    }

    @Override
    public void onClose(Session session) {
        removeSubscription();
    }

    protected void removeSubscription() {
        if (subscribed == -1) {
            return;
        }
        if (!connected.containsKey(subscribed)) {
            return;
        }
        connected.get(subscribed).remove(this);
        subscribed = -1;
    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }
}
