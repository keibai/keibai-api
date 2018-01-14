package main.java.socket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.ProjectVariables;
import main.java.combinatorial.KAuctionSolver;
import main.java.combinatorial.KBid;
import main.java.dao.*;
import main.java.dao.sql.AuctionDAOSQL;
import main.java.dao.sql.BidDAOSQL;
import main.java.dao.sql.GoodDAOSQL;
import main.java.dao.sql.UserDAOSQL;
import main.java.dao.sql.EventDAOSQL;
import main.java.gson.BetterGson;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.Good;
import main.java.models.Event;
import main.java.models.User;
import main.java.models.meta.BodyWS;
import main.java.models.meta.Msg;
import main.java.utils.HttpSession;
import main.java.utils.JsonCommon;
import main.java.utils.Logger;

import javax.websocket.Session;
import java.sql.Timestamp;
import java.util.*;
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
    public static final String SOME_AUCTION_PENDING = "Review pending auctions first.";
    public static final String EVENT_FINISHED = "Can not start an auction on a finished event.";
    public static final String EVENT_NOT_IN_PROGRESS = "Cannot close an event which is not in progress.";
    public static final String DIFFERENT_AMOUNTS = "Bids contain different amounts";
    public static final String DIFFERENT_AUCTIONS = "Bids contain different auctions";
    public static final String USER_ALREADY_BIDDED = "Can only bid once in a combinatorial auction";

    public static final String TYPE_AUCTION_SUBSCRIBE = "AuctionSubscribe";
    public static final String TYPE_AUCTION_CONNECTIONS_ONCE = "AuctionConnectionOnce";
    public static final String TYPE_AUCTION_NEW_CONNECTION = "AuctionNewConnection";
    public static final String TYPE_AUCTION_NEW_DISCONNECTION = "AuctionNewDisconnection";
    public static final String TYPE_AUCTION_BID = "AuctionBid";
    public static final String TYPE_AUCTION_BIDDED = "AuctionBidded";
    public static final String TYPE_AUCTION_START = "AuctionStart";
    public static final String TYPE_AUCTION_STARTED = "AuctionStarted";
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
            case TYPE_AUCTION_CONNECTIONS_ONCE: {
                onAuctionSubscribersOnce(body);
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
            unsafeAuction = new BetterGson().newInstance().fromJson(body.json, Auction.class);
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

    /**
     * TYPE_AUCTION_CONNECTIONS_ONCE
     */

    protected void onAuctionSubscribersOnce(BodyWS body) {
        if (subscribed == -1) {
            String json = JsonCommon.error(SUBSCRIPTION_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        Msg subscribers = new Msg();
        subscribers.msg = String.valueOf(connected.get(subscribed).size());
        String json = new BetterGson().newInstance().toJson(subscribers);
        sender.reply(session, body, BodyWSCommon.ok(json));
    }

    /**
     * TYPE_AUCTION_BID
     */

    protected void onAuctionBid(BodyWS body) {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();

        int userId = httpSession.userId();
        if (userId == -1) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        Bid[] unsafeBids;
        try {
            unsafeBids = new BetterGson().newInstance().fromJson(body.json, Bid[].class);
        } catch (JsonSyntaxException e) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeBids == null || unsafeBids.length == 0) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        Bid firstBid = unsafeBids[0];

        Auction dbAuction;
        try {
            dbAuction = auctionDAO.getById(firstBid.auctionId);
        } catch (DAOException e) {
            Logger.error("Get auction by ID", String.valueOf(firstBid.auctionId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbAuction == null) {
            String json = JsonCommon.error(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        Event dbEvent;
        try {
            dbEvent = eventDAO.getById(dbAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Get event by ID", String.valueOf(dbAuction.eventId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        switch (dbEvent.auctionType) {
            case Event.ENGLISH:
                onEnglishAuctionBid(body, firstBid, dbAuction, userId);
                break;
            case Event.COMBINATORIAL:
                onCombinatorialAuctionBid(body, unsafeBids, dbAuction, userId);
                break;
        }
    }

    private void onEnglishAuctionBid(BodyWS body, Bid unsafeBid, Auction dbAuction, int userId) {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();

        unsafeBid.amount = Math.floor(unsafeBid.amount * 100) / 100;
        if (unsafeBid.amount <= 0.1) {
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

        dbAuction.maxBid = dbBid.amount;
        try {
            auctionDAO.update(dbAuction);
        } catch (DAOException e) {
            Logger.error("Update auction", dbAuction.toString(), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        String json = new BetterGson().newInstance().toJson(dbBid);
        sender.reply(session, body, BodyWSCommon.ok(json));

        // Broadcast bid to everyone in the auction.
        auctionBidded(dbBid);
    }

    private void onCombinatorialAuctionBid(BodyWS body, Bid[] unsafeBids, Auction dbAuction, int userId) {
        BidDAO bidDAO = BidDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();

        List<Bid> unsafeBidsList = new ArrayList<>(Arrays.asList(unsafeBids));

        // 1. All bids must have the same auction ID and amount.
        double commonAmount = Math.floor(unsafeBids[0].amount * 100) / 100;
        int commonAuctionId = unsafeBids[0].auctionId;
        Set<Integer> differentGoods = new HashSet<>();
        for (Bid unsafeBid: unsafeBidsList) {
            unsafeBid.amount = Math.floor(unsafeBid.amount * 100) / 100;
            if (unsafeBid.amount <= 1.0) {
                String json = JsonCommon.error(INVALID_AMOUNT_ERROR);
                sender.reply(session, body, BodyWSCommon.error(json));
                return;
            }
            if (Double.compare(commonAmount, unsafeBid.amount) != 0) {
                String json = JsonCommon.error(DIFFERENT_AMOUNTS);
                sender.reply(session, body, BodyWSCommon.error(json));
                return;
            }
            if (commonAuctionId != unsafeBid.auctionId) {
                String json = JsonCommon.error(DIFFERENT_AUCTIONS);
                sender.reply(session, body, BodyWSCommon.error(json));
                return;
            }
            if (unsafeBid.goodId == 0) {
                String json = JsonCommon.error(GOOD_ID_ERROR);
                sender.reply(session, body, BodyWSCommon.error(json));
                return;
            }
            // 1.1. Remove duplicated bids to the same good
            if (differentGoods.contains(unsafeBid.goodId)) {
                unsafeBidsList.remove(unsafeBid);
            } else {
                differentGoods.add(unsafeBid.goodId);
                //1.2. Check good exists
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
            }
        }

        // 2. Check auction. At this point commonAuctionId == dbAuction.id
        if (commonAuctionId == 0) {
            String json = JsonCommon.error(AUCTION_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (commonAuctionId != subscribed) {
            String json = JsonCommon.error(SUBSCRIPTION_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (!dbAuction.status.equals(Auction.IN_PROGRESS)) {
            String json = JsonCommon.error(AUCTION_NOT_IN_PROGRESS);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // 3. User checks
        User dbUser;
        try {
            dbUser = userDAO.getById(userId);
        } catch (DAOException e) {
            Logger.error("Bid get user by ID at combinatorial auction", String.valueOf(userId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        // 3.1. User hasn't any bid on this auction
        Set<Bid> userBids;
        try {
            userBids = new HashSet<>(bidDAO.getListByOwnerId(userId));
        } catch (DAOException e) {
            Logger.error("Bid get user bids at combinatorial auction", String.valueOf(userId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }
        Set<Bid> auctionBids;
        try {
            auctionBids = new HashSet<>(bidDAO.getListByAuctionId(commonAuctionId));
        } catch (DAOException e) {
            Logger.error("Bid get auction bids at combinatorial auction", String.valueOf(commonAuctionId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }
        userBids.retainAll(auctionBids);
        if (userBids.size() != 0) {
            String json = JsonCommon.error(USER_ALREADY_BIDDED);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // 3.2. User has enough credit
        if (dbUser.credit < commonAmount) {
            String json = JsonCommon.error(NO_CREDIT);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        // Al checks passed ( @zurfyx likes this part ;) )
        Bid dbBid = null;
        for (Bid unsafeBid: unsafeBidsList) {
            Bid newBid = new Bid();
            newBid.amount = unsafeBid.amount;
            newBid.auctionId = unsafeBid.auctionId;
            newBid.goodId = unsafeBid.goodId;
            newBid.ownerId = this.httpSession.userId();

            try {
                dbBid = bidDAO.create(newBid);
            } catch (DAOException e) {
                Logger.error("Create bid", newBid.toString(), e.toString());
                sender.reply(session, body, BodyWSCommon.internalServerError());
                return;
            }
        }

        // In this case we protect the bid amount
        dbBid.amount = 0.0;
        String json = new BetterGson().newInstance().toJson(dbBid);
        sender.reply(session, body, BodyWSCommon.ok(json));

        // Broadcast bid to everyone in the auction.
        auctionBidded(dbBid);
    }

    protected void auctionBidded(Bid newBid) {
        BodyWS body = new BodyWS();
        body.type = TYPE_AUCTION_BIDDED;
        body.json = new BetterGson().newInstance().toJson(newBid);

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
            unsafeAuction = new BetterGson().newInstance().fromJson(body.json, Auction.class);
        } catch (JsonSyntaxException e) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeAuction.id == 0) {
            String json = JsonCommon.msg(AUCTION_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
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

        // Can't start if not all auctions are set as ACCEPTED.
        List<Auction> dbEventAuctions;
        try {
            dbEventAuctions = auctionDAO.getListByEventId(dbEvent.id);
        } catch (DAOException e) {
            Logger.error("Get event auctions", String.valueOf(dbEvent.id), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        List<Auction> unacceptedAuctions = dbEventAuctions.stream().filter(auction -> !auction.status.equals(Auction.ACCEPTED)).collect(Collectors.toList());
        if (unacceptedAuctions.size() > 0) {
            String json = JsonCommon.error(SOME_AUCTION_PENDING);
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

        String jsonAuction = new BetterGson().newInstance().toJson(dbAuction);
        sender.reply(session, body, BodyWSCommon.ok(jsonAuction));

        auctionStarted(dbAuction);
    }

    protected void auctionStarted(Auction auction) {
        BodyWS body = new BodyWS();
        body.type = TYPE_AUCTION_STARTED;
        body.status = 200;
        body.json = new BetterGson().newInstance().toJson(auction);

        List<Session> sessions = connected.get(auction.id).parallelStream().map(b -> b.session).collect(Collectors.toList());
        sender.send(sessions, body);
    }

    /**
     * TYPE_AUCTION_CLOSE
     */

    public void onAuctionClose(BodyWS body) {
        AuctionDAO auctionDAO = AuctionDAOSQL.getInstance();
        EventDAO eventDAO = EventDAOSQL.getInstance();
        BidDAO bidDAO = BidDAOSQL.getInstance();
        GoodDAO goodDAO = GoodDAOSQL.getInstance();
        UserDAO userDAO = UserDAOSQL.getInstance();

        int userId = httpSession.userId();
        if (userId == -1) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        Auction unsafeAuction;
        try {
            unsafeAuction = new BetterGson().newInstance().fromJson(body.json, Auction.class);
        } catch (JsonSyntaxException e) {
            sender.reply(session, body, BodyWSCommon.invalidRequest());
            return;
        }

        if (unsafeAuction.id == 0) {
            String json = JsonCommon.msg(AUCTION_ID_ERROR);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
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
            Logger.error("Retrieve auction on auction close", String.valueOf(unsafeAuction.id), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbAuction == null) {
            String json = JsonCommon.msg(AUCTION_DOES_NOT_EXIST);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        if (!dbAuction.status.equals(Auction.IN_PROGRESS)) {
            String json = JsonCommon.error(WRONG_AUCTION_STATUS);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        Event dbEvent;
        try {
            dbEvent = eventDAO.getById(dbAuction.eventId);
        } catch (DAOException e) {
            Logger.error("Retrieve event on auction close", String.valueOf(dbAuction.eventId), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        if (dbEvent.ownerId != userId) {
            sender.reply(session, body, BodyWSCommon.unauthorized());
            return;
        }

        if (!dbEvent.status.equals(Event.IN_PROGRESS)) {
            String json = JsonCommon.error(EVENT_NOT_IN_PROGRESS);
            sender.reply(session, body, BodyWSCommon.error(json));
            return;
        }

        /*
         All checks passed.

         1. Auction will set as FINISHED.
         2. Auction ending time will be set.
         3. If all event auctions were set as FINISHED, event will be set finished too.
         4 English. Auction winner(s) will be determined:
           3.0. If no bids, no auction winner. Skip step.
           3.1. Auction winner will be set.
           3.2. Auction winner (MAX_BIDDER) will be deduced MAX_BID from their credit.
           3.3. Good owner will be credited a (1 - HOUSE_FEE - EVENT_OWNER_FEE) * MAX_BID.
           3.4. Event owner will be credited a EVENT_OWNER_FEE * MAX_BID.

         */

        // 1.
        dbAuction.status = Auction.FINISHED;

        // 2.
        dbAuction.endingTime = new Timestamp(System.currentTimeMillis());

        // 3.
        List<Bid> auctionBids;

        try {
            // 3.0.
            auctionBids = bidDAO.getListByAuctionId(dbAuction.id);
        } catch (DAOException e) {
            Logger.error("Retrieve list of bids on auction close", dbAuction.toString(), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        switch (dbEvent.auctionType) {
            case Event.ENGLISH: {
                if (auctionBids.size() > 0) {
                    // 3.1.
                    Bid maxBid = Collections.max(auctionBids);
                    dbAuction.winnerId = maxBid.ownerId;

                    // 3.2.
                    User winnerUser;
                    try {
                        winnerUser = userDAO.getById(maxBid.ownerId);
                    } catch (DAOException e) {
                        Logger.error("Retrieve winner of auction on auction close", maxBid.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }
                    winnerUser.credit -= maxBid.amount;
                    try {
                        userDAO.update(winnerUser);
                    } catch (DAOException e) {
                        Logger.error("Update winner of auction on auction close", winnerUser.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }

                    // 3.3.
                    User goodOwner;
                    try {
                        goodOwner = userDAO.getById(dbAuction.ownerId);
                    } catch (DAOException e) {
                        Logger.error("Retrieve owner of good on auction close", maxBid.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }
                    goodOwner.credit += (1 - ProjectVariables.EVENT_OWNER_FEE - ProjectVariables.HOUSE_FEE) * maxBid.amount;
                    try {
                        userDAO.update(goodOwner);
                    } catch (DAOException e) {
                        Logger.error("Update owner of good on auction close", goodOwner.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }

                    // 3.4.
                    User eventOwner;
                    try {
                        eventOwner = userDAO.getById(dbEvent.ownerId);
                    } catch (DAOException e) {
                        Logger.error("Retrieve owner of event on auction close", dbEvent.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }
                    eventOwner.credit += ProjectVariables.EVENT_OWNER_FEE * maxBid.amount;
                    try {
                        userDAO.update(eventOwner);
                    } catch (DAOException e) {
                        Logger.error("Update owner of event on auction close", eventOwner.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }
                }

                break;
            }
            case Event.COMBINATORIAL: {
                if (auctionBids.size() > 0) {
                    Map<Integer, List<Integer>> goodIds = new HashMap<>(); // User, Good ID
                    Map<Integer, Double> valueGoods = new HashMap<>(); // User, Good value
                    for (Bid bid: auctionBids) {
                        int bidUserId = bid.ownerId;
                        int bidGoodId = bid.goodId;
                        double bidValueGood = bid.amount;

                        if (!goodIds.containsKey(bidUserId)) {
                            goodIds.put(bidUserId, new ArrayList<>());
                        }
                        goodIds.get(bidUserId).add(bidGoodId);

                        valueGoods.put(bidUserId, bidValueGood);
                    }

                    List<KBid> kBids = new ArrayList<>();
                    for (Map.Entry<Integer, List<Integer>> entry: goodIds.entrySet()) {
                        Integer entryUserId = entry.getKey();
                        int[] entryGoodIds = entry.getValue().stream().mapToInt(i->i).toArray();
                        Double entryAmountDouble = valueGoods.get(entryUserId);
                        int entryAmount = entryAmountDouble.intValue();
                        KBid kBid = new KBid(entryUserId, entryAmount, entryGoodIds);
                        kBids.add(kBid);
                    }

                    KBid[] kBidsArr = kBids.toArray(new KBid[kBids.size()]);
                    KAuctionSolver kAuctionSolver = new KAuctionSolver(kBidsArr);
                    List<KBid> kBidWinners = kAuctionSolver.solve();

                    // Retrieve good owner
                    User goodOwner;
                    try {
                        goodOwner = userDAO.getById(dbAuction.ownerId);
                    } catch (DAOException e) {
                        Logger.error("Retrieve owner of good on auction close", dbAuction.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }

                    // Retrieve event owner
                    User eventOwner;
                    try {
                        eventOwner = userDAO.getById(dbEvent.ownerId);
                    } catch (DAOException e) {
                        Logger.error("Retrieve owner of event on auction close", dbEvent.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }

                    // Iterate winner bids
                    String winners = "";
                    String separator = "";
                    for (KBid winnerBid: kBidWinners) {
                        // Discount credit to winners
                        User winnerUser;
                        try {
                            winnerUser = userDAO.getById(winnerBid.id);
                        } catch (DAOException e) {
                            Logger.error("Retrieve winner of auction on auction close", winnerBid.toString(), e.toString());
                            sender.reply(session, body, BodyWSCommon.internalServerError());
                            return;
                        }
                        winnerUser.credit -= winnerBid.value;
                        // Update winners credit
                        try {
                            userDAO.update(winnerUser);
                        } catch (DAOException e) {
                            Logger.error("Update winner of auction on auction close", winnerUser.toString(), e.toString());
                            sender.reply(session, body, BodyWSCommon.internalServerError());
                            return;
                        }
                        // Add credit to good owner
                        goodOwner.credit += (1 - ProjectVariables.EVENT_OWNER_FEE - ProjectVariables.HOUSE_COMB_FEE) * winnerBid.value;
                        // Add credit to event owner
                        eventOwner.credit += ProjectVariables.EVENT_OWNER_FEE * winnerBid.value;
                        // Change auction combinatorial winners
                        winners += separator + winnerBid.id;
                        separator = ",";
                    }
                    // Update good owner
                    try {
                        userDAO.update(goodOwner);
                    } catch (DAOException e) {
                        Logger.error("Update owner of good on auction close", goodOwner.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }

                    // Update event owner
                    try {
                        userDAO.update(eventOwner);
                    } catch (DAOException e) {
                        Logger.error("Update owner of event on auction close", eventOwner.toString(), e.toString());
                        sender.reply(session, body, BodyWSCommon.internalServerError());
                        return;
                    }

                    dbAuction.combinatorialWinners = winners;
                }

                break;
            }
        }

        // 4.
        try {
            dbAuction = auctionDAO.update(dbAuction);
        } catch (DAOException e) {
            Logger.error("Update auction on auction close", dbAuction.toString(), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }

        List<Auction> eventAuctionList;
        try {
            eventAuctionList = auctionDAO.getListByEventId(dbEvent.id);
        } catch (DAOException e) {
            Logger.error("Retrieve list of auctions on auction close", dbEvent.toString(), e.toString());
            sender.reply(session, body, BodyWSCommon.internalServerError());
            return;
        }
        boolean hasAllAuctionsFinished = true;
        for (Auction a: eventAuctionList) {
            if (!a.status.equals(Auction.FINISHED)) {
                hasAllAuctionsFinished = false;
                break;
            }
        }
        if (hasAllAuctionsFinished) {
            dbEvent.status = Event.FINISHED;
            try {
                eventDAO.update(dbEvent);
            } catch (DAOException e) {
                Logger.error("Update event on auction close", dbEvent.toString(), e.toString());
                sender.reply(session, body, BodyWSCommon.internalServerError());
                return;
            }
        }

        String jsonAuction = new BetterGson().newInstance().toJson(dbAuction);
        sender.reply(session, body, BodyWSCommon.ok(jsonAuction));

        auctionClosed(dbAuction);
    }

    protected void auctionClosed(Auction auction) {
        BodyWS body = new BodyWS();
        body.type = TYPE_AUCTION_CLOSED;
        body.status = 200;
        body.json = new BetterGson().newInstance().toJson(auction);

        List<Session> sessions = connected.get(auction.id).parallelStream().map(b -> b.session).collect(Collectors.toList());
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

        User idOnlyUser = new User();
        idOnlyUser.id = httpSession.userId();
        connected.get(subscribed).remove(this);
        newDisconnection(subscribed, idOnlyUser);
        subscribed = -1;
    }

    private void connectionBroadcast(String bodyType, int auctionId, User user) {
        User broadcastUser = user.clone();
        broadcastUser.password = null;
        broadcastUser.credit = 0.0;

        BodyWS body = new BodyWS();
        body.type = bodyType;
        body.status = 200;
        body.json = new BetterGson().newInstance().toJson(broadcastUser);

        List<Session> sessions = connected.get(auctionId).parallelStream().map(b -> b.session).collect(Collectors.toList());
        sender.send(sessions, body);
    }

    protected void newConnection(int auctionId, User user) {
        connectionBroadcast(TYPE_AUCTION_NEW_CONNECTION, auctionId, user);
    }

    protected void newDisconnection(int auctionId, User user) {
        connectionBroadcast(TYPE_AUCTION_NEW_DISCONNECTION, auctionId, user);
    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }

    static void clearConnected() {
        connected.clear();
    }
}
