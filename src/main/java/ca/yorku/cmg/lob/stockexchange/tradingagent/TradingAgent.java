package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.Event;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

public abstract class TradingAgent {
    protected Trader t;
    protected StockExchange exc;
    protected NewsBoard news;
    protected ITradingStrategy strategy;

    public TradingAgent(Trader t, StockExchange e, NewsBoard n, ITradingStrategy strategy) {
        this.t = t;
        this.exc = e;
        this.news = n;
        this.strategy = strategy;
    }

    public void timeAdvancedTo(long time) {
        pollForEvents(time);
    }

    private void examineEvent(Event e) {
        int positionInSecurity = exc.getAccounts().getTraderAccount(t).getPosition(e.getSecrity().getTicker());
        if (positionInSecurity > 0) {
            strategy.actOnEvent(this, e, positionInSecurity, exc.getPrice(e.getSecrity().getTicker()));
        }
    }

    private void pollForEvents(long time) {
        Event e = news.getEventAt(time);
        if (e != null) {
            examineEvent(e);
        }
    }

    public Trader getTrader() { return t; }
    public StockExchange getExchange() { return exc; }
    public NewsBoard getNewsBoard() { return news; }
}
