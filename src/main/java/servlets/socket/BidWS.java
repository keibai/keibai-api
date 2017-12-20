package main.java.servlets.socket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.models.Auction;
import main.java.models.Bid;
import main.java.models.meta.MsgWS;
import main.java.utils.HttpSession;


import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BidWS implements WS {

    private static final Map<Integer, Set<BidWS>> connected = new ConcurrentHashMap<>();

    HttpSession httpSession;

    @Override
    public void onOpen(Session session, main.java.utils.HttpSession httpSession) {
        this.httpSession = httpSession;
    }

    @Override
    public void onMessage(Session session, MsgWS message) {
        switch(message.type) {
            case "AuctionSubscribe": {
                onAuctionSubscribe(session, message);
                break;
            }
        }
    }

    protected void onAuctionSubscribe(Session session, MsgWS message) {
        Auction unsafeAuction;
        try {
            unsafeAuction = (Auction) message.object;
        } catch (ClassCastException e) {
            System.out.println("error");
            System.out.println(e);
            return;
        }
        System.out.println(unsafeAuction);
        System.out.println("ya, it's fine");
//        connected.getOrDefault(unsafeBid)
    }

    @Override
    public void onClose(Session session) {

    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }
}
