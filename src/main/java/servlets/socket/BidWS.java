package main.java.servlets.socket;

import com.google.gson.Gson;
import main.java.models.Auction;
import main.java.models.meta.BodyWS;
import main.java.utils.HttpSession;


import javax.websocket.Session;
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
    public void onMessage(Session session, BodyWS body) {
        switch(body.type) {
            case "AuctionSubscribe": {
                onAuctionSubscribe(session, body);
                break;
            }
        }
    }

    protected void onAuctionSubscribe(Session session, BodyWS body) {
        Auction unsafeAuction = new Gson().fromJson(body.json, Auction.class);
        System.out.println(unsafeAuction);
        System.out.println("auction subscribe");
//        connected.getOrDefault(unsafeBid)
    }

    @Override
    public void onClose(Session session) {

    }

    @Override
    public void onError(Session session, Throwable throwable) {

    }
}
