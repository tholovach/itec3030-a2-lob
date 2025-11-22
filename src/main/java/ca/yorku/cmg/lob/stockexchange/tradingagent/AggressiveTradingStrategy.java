package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.orderbook.Ask;
import ca.yorku.cmg.lob.orderbook.Bid;
import ca.yorku.cmg.lob.stockexchange.events.BadNews;
import ca.yorku.cmg.lob.stockexchange.events.Event;
import ca.yorku.cmg.lob.stockexchange.events.GoodNews;
import ca.yorku.cmg.lob.tradestandards.IOrder;

// More aggressive trading strategy - submits larger orders and takes bigger risks

public class AggressiveTradingStrategy implements ITradingStrategy {
    @Override
    public void actOnEvent(TradingAgent agent, Event e, int pos, int price) {
        IOrder newOrder = null;
        
        if (e instanceof GoodNews) {
            newOrder = new Bid(agent.getTrader(), e.getSecrity(), (int) Math.round(price * 1.05), (int) Math.round(pos * 0.5), e.getTime());
        } else if (e instanceof BadNews) {
            newOrder = new Ask(agent.getTrader(), e.getSecrity(), (int) Math.round(price * 0.90), (int) Math.round(pos * 0.8), e.getTime());
        } else {
            System.out.println("Unknown event type");
        }
        
        if (newOrder != null) {
            agent.getExchange().submitOrder(newOrder, e.getTime());
        }
    }
}