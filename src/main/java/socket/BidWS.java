package main.java.socket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.dao.AuctionDAO;
import main.java.dao.BidDAO;
import main.java.dao.DAOException;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.models.Auction;
import main.java.models.Bid;
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

public class BidWS implements WS {

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
        synchronized (connected) {
            if (!connected.containsKey(dbAuction.id)) {
                connected.put(dbAuction.id, ConcurrentHashMap.newKeySet());
            }
        }
        connected.get(dbAuction.id).add(this);
        subscribed = dbAuction.id;
        System.out.println(dbAuction);

        BodyWS okBody = new BodyWS();
        okBody.status = 200;
        okBody.json = JsonCommon.ok();
        sender.reply(session, body, okBody);
    }

    protected void onAuctionBid(BodyWS body) {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        int userId = httpSession.userId();
        if (userId == -1) {
            BodyWS unauthorizedBody = new BodyWS();
            unauthorizedBody.status = 400;
            unauthorizedBody.json = JsonCommon.unauthorized();
            sender.reply(session, body, unauthorizedBody);
            return;
        }

        Bid unsafeBid;
        try {
            unsafeBid = new Gson().fromJson(body.json, Bid.class);
        } catch (JsonSyntaxException e) {
            // jsonResponse.invalidRequest();
            System.out.println("Invalid request.");
            return;
        }

        if (unsafeBid == null) {
            // jsonResponse.invalidRequest();
            System.out.println("Invalid request.");
            return;
        }

        if (unsafeBid.amount <= 0.0) {
            // jsonResponse.error(INVALID_AMOUNT_ERROR);
            System.out.println("Invalid amount.");
            return;
        }

        if (unsafeBid.auctionId == 0) {
            // jsonResponse.error(AUCTION_ID_ERROR);
            System.out.println("Empty auction ID.");
            return;
        }

        // TODO test
        if (unsafeBid.auctionId != subscribed) {
            System.out.println("Not subscribed to that auction.");
            return;
        }

        Auction auction;
        try {
            auction = auctionDAO.getById(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID " + unsafeBid.auctionId, e.toString());
            // jsonResponse.internalServerError();
            return;
        }

        if (auction == null) {
            // jsonResponse.error(AUCTION_NOT_EXIST_ERROR);
            System.out.println("Auction " + unsafeBid.auctionId + " does not exist");
            return;
        }

        // TODO: Test!
        if (!auction.status.equals(Auction.IN_PROGRESS)) {
            System.out.println("Auction " + unsafeBid.auctionId + " not in progress");
            //jsonResponse.error(AUCTION_NOT_IN_PROGRESS);
            return;
        }

        // TODO: Test!
        List<Bid> auctionBids;
        try {
            auctionBids = bidDAO.getListByAuctionId(unsafeBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get bid list by auction ID " + unsafeBid.auctionId, e.toString());
            //jsonResponse.internalServerError();
            return;
        }

        // TODO: Test
        if (auctionBids.size() > 0) {
            Bid maxBid = Collections.max(auctionBids);
            if ((unsafeBid.amount - maxBid.amount) <= 0) {
                //jsonResponse.error(BID_NOT_HIGHER_ENOUGH);
                System.out.println("Bid of amount " + unsafeBid.amount + " no higher enough. Bid should be higher than " + maxBid.amount);
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
            // jsonResponse.internalServerError();
            return;
        }


        auctionBidded(dbBid);
        System.out.println(dbBid);

    }

    protected void auctionBidded(Bid newBid) {
        System.out.println("returning");
        System.out.println(newBid);
        BodyWS body = new BodyWS();
        body.type = "AuctionBidded";
        body.nonce = "1";
        body.json = new Gson().toJson(newBid);
        for (BidWS endpoint : connected.get(newBid.auctionId)) {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().sendObject(body);
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        }
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
    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }
}
