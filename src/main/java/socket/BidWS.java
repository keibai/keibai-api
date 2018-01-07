package main.java.socket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.*;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Good;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.BodyWS;
import main.java.utils.HttpSession;
import main.java.utils.JsonCommon;
import main.java.utils.Logger;

import javax.websocket.Session;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BidWS implements WS {

    public static final String INVALID_AMOUNT_ERROR = "Invalid amount";
    public static final String AUCTION_ID_ERROR = "Missing auction ID.";
    public static final String GOOD_ID_ERROR = "Missing good ID.";
    public static final String SUBSCRIPTION_ERROR = "User not found inside the auction.";
    public static final String NO_CREDIT = "Not enough credit.";
    public static final String LOW_BID_STARTING_PRICE = "Bid cannot be lower than initial starting price";
    public static final String AUCTION_DOES_NOT_EXIST = "Auction does not exist";
    public static final String GOOD_DOES_NOT_EXIST = "Good does not exist";
    public static final String AUCTION_NOT_IN_PROGRESS = "Auction is not in progress";
    public static final String LOW_BID_HIGHER_BID = "You cannot bid lower than the highest bid.";
    public static final String HAS_BIDDED_IN_IN_PROGRESS_AUCTION_TRYING_TO_BID_ANOTHER = "You are currently bidding in another auction.";
    public static final String WRONG_AUCTION_STATUS = "Wrong auction status.";
    public static final String EVENT_FINISHED = "Can not start an auction on a finished event.";

    public static final String TYPE_AUCTION_SUBSCRIBE = "AuctionSubscribe";
    public static final String TYPE_AUCTION_NEW_CONNECTION = "AuctionNewConnection";
    public static final String TYPE_AUCTION_BID = "AuctionBid";
    public static final String TYPE_AUCTION_BIDDED = "AuctionBidded";
    public static final String TYPE_AUCTION_START = "AuctionOpen";
    public static final String TYPE_AUCTION_STARTED = "AuctionOpened";
    public static final String TYPE_AUCTION_CLOSE = "AuctionClose";
    public static final String TYPE_AUCTION_CLOSED = "AuctionClosed";

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
            case TYPE_AUCTION_START: {
                onAuctionStart(body);
                break;
            }
            case TYPE_AUCTION_CLOSE: {
                onAuctionClose(body);
                break;
            }
        }
    }

    /**
     * TYPE_AUCTION_SUBSCRIBE
     */

    protected void onAuctionSubscribe(BodyWS body) {
        UserDAO userDAO = UserDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        int userId = httpSession.userId();
        if (userId == -1) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        User dbUser;
        try {
            dbUser = userDAO.getById(userId);
        } catch (DAOException e) {
            Logger.error("Subscribe get user by ID", String.valueOf(userId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        Auction unsafeAuction;
        try {
            unsafeAuction = new Gson().fromJson(body.json, Auction.class);
        } catch (JsonSyntaxException e) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeAuction.id == 0) {
            String json = JsonCommon.error(AUCTION_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(unsafeAuction.id);
        } catch (DAOException e) {
            Logger.error("Get auction by ID", String.valueOf(unsafeAuction.id), e.toString());
            String json = JsonCommon.error(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }
        if (dbAuction == null) {
            String json = JsonCommon.error(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // Add session to connected sessions to that auction.
        removeSubscription();
        subscribed = dbAuction.id;
        synchronized (connected) {
            if (!connected.containsKey(subscribed)) {
                connected.put(subscribed, ConcurrentHashMap.newKeySet());
            }
        }
        connected.get(subscribed).add(this);

        BodyWS okBody = new BodyWS();
        okBody.status = 200;
        okBody.json = JsonCommon.ok();
        sender.reply(session, body, okBody);

        newConnection(dbAuction.id, dbUser);
    }

    protected void newConnection(int auctionId, User user) {
        User broadcastUser = user.clone();
        broadcastUser.password = null;
        broadcastUser.credit = 0.0;

        BodyWS body = new BodyWS();
        body.type = TYPE_AUCTION_NEW_CONNECTION;
        body.status = 200;
        body.json = new Gson().toJson(broadcastUser);

        List<Session> sessions = connected.get(auctionId).parallelStream().map(b -> b.session).collect(Collectors.toList());
        sender.send(sessions, body);
    }

    /**
     * TYPE_AUCTION_BID
     */

    protected void onAuctionBid(BodyWS body) {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

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

        if (unsafeBid.auctionId != subscribed) {
            String json = JsonCommon.error(SUBSCRIPTION_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (unsafeBid.goodId == 0) {
            String json = JsonCommon.error(GOOD_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        User dbUser;
        try {
            dbUser = userDAO.getById(userId);
        } catch (DAOException e) {
            Logger.error("Bid get user by ID", String.valueOf(userId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID", String.valueOf(unsafeBid.auctionId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbAuction == null) {
            String json = JsonCommon.error(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (!dbAuction.status.equals(Auction.IN_PROGRESS)) {
            String json = JsonCommon.error(AUCTION_NOT_IN_PROGRESS);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        Good dbGood;
        try {
            dbGood = goodDAO.getById(unsafeBid.goodId);
        } catch (DAOException e) {
            Logger.error("Get good by ID", String.valueOf(unsafeBid.goodId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbGood == null) {
            String json = JsonCommon.error(GOOD_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        List<Bid> dbAuctionBids;
        try {
            dbAuctionBids = bidDAO.getListByAuctionId(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get bid list by auction ID", String.valueOf(unsafeBid.auctionId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        // User bid amount has to be higher or equal than auction starting price.
        if (unsafeBid.amount < dbAuction.startingPrice) {
            String json = JsonCommon.error(LOW_BID_STARTING_PRICE);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // User has enough credit
        if (dbUser.credit < unsafeBid.amount) {
            String json = JsonCommon.error(NO_CREDIT);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // User bid has to be higher than the maximum bid for that auction.
        if (dbAuctionBids.size() > 0) {
            Bid maxBid = Collections.max(dbAuctionBids);
            if ((unsafeBid.amount - maxBid.amount) <= 0) {
                String json = JsonCommon.error(LOW_BID_HIGHER_BID);
                sender.reply(session, body, BodyWSCommon.error(json));
                return;
            }
        }

        // User can't be bidding on another auction.
        Auction dbAuctionIsBidding;
        try {
            dbAuctionIsBidding = auctionDAO.getAuctionWhereUserIsBidding(userId);
        } catch (DAOException e) {
            Logger.error("Get auction where user is bidding", String.valueOf(unsafeBid.auctionId), String.valueOf(userId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbAuctionIsBidding != null && dbAuctionIsBidding.id != dbAuction.id) {
            String json = JsonCommon.error(HAS_BIDDED_IN_IN_PROGRESS_AUCTION_TRYING_TO_BID_ANOTHER);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        Bid newBid = new Bid();
        newBid.amount = unsafeBid.amount;
        newBid.auctionId = unsafeBid.auctionId;
        newBid.goodId = unsafeBid.goodId;
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
        BodyWS body = new BodyWS();
        body.type = TYPE_AUCTION_BIDDED;
        body.json = new Gson().toJson(newBid);

        List<Session> sessions = connected.get(newBid.auctionId).parallelStream().map(b -> b.session).collect(Collectors.toList());
        sender.send(sessions, body);
    }

    /**
     * TYPE_AUCTION_START
     */

    public void onAuctionStart(BodyWS body) {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = httpSession.userId();
        if (userId == -1) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        Auction unsafeAuction;
        try {
            unsafeAuction = new Gson().fromJson(body.json, Auction.class);
        } catch (JsonSyntaxException e) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeAuction.id == 0) {
            String json = JsonCommon.msg(AUCTION_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.ok());
        }

        if (unsafeAuction.id != subscribed) {
            String json = JsonCommon.error(SUBSCRIPTION_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // Retrieve auction
        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(unsafeAuction.id);
        } catch (DAOException e) {
            Logger.error("Retrieve auction on auction start", String.valueOf(unsafeAuction.id), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbAuction == null) {
            String json = JsonCommon.msg(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (!dbAuction.status.equals(Auction.ACCEPTED)) {
            String json = JsonCommon.error(WRONG_AUCTION_STATUS);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // Retrieve event
        Event dbEvent;
        try {
            dbEvent = eventDAO.getById(dbAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Retrieve event on auction start", String.valueOf(dbAuction.eventId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbEvent.ownerId != userId) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        if (dbEvent.status.equals(Event.FINISHED)) {
            String json = JsonCommon.error(EVENT_FINISHED);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // Update event status
        dbEvent.status = Event.IN_PROGRESS;
        try {
            eventDAO.update(dbEvent);
        } catch (DAOException e) {
            Logger.error("Update event on auction start", String.valueOf(dbEvent.id), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        // Update auction status and starting time
        dbAuction.status = Auction.IN_PROGRESS;
        dbAuction.startTime = new Timestamp(System.currentTimeMillis());
        try {
            auctionDAO.update(dbAuction);
        } catch (DAOException e) {
            Logger.error("Update auction on auction start", String.valueOf(dbAuction.id), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        String jsonAuction = new Gson().toJson(dbAuction);
        sender.reply(session, body, BodyWSCommon.ok(jsonAuction));

        auctionStarted(dbAuction);
    }

    protected void auctionStarted(Auction auction) {
        BodyWS body = new BodyWS();
        body.type = TYPE_AUCTION_STARTED;
        body.status = 200;
        body.json = new Gson().toJson(auction);

        List<Session> sessions = connected.get(auction.id).parallelStream().map(b -> b.session).collect(Collectors.toList());
        sender.send(sessions, body);
    }

    /**
     * TYPE_AUCTION_CLOSE
     */

    public void onAuctionClose(BodyWS body) {

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

    static void clearConnected() {
        connected.clear();
    }
}
