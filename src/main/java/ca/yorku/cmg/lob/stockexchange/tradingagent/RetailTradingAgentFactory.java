package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

public class RetailTradingAgentFactory {
    public TradingAgent createAgent(Trader t, StockExchange e, NewsBoard n, String style) {
        ITradingStrategy strategy = style.equalsIgnoreCase("Conservative") ?
            new ConservativeTradingStrategy() : new AggressiveTradingStrategy();
        return new TradingAgentRetail(t, e, n, strategy);
    }
}