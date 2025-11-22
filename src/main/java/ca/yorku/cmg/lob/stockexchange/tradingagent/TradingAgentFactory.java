package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

public class TradingAgentFactory extends AbstractTradingAgentFactory {
    private InstitutionalTradingAgentFactory institutionalFactory = new InstitutionalTradingAgentFactory();
    private RetailTradingAgentFactory retailFactory = new RetailTradingAgentFactory();

    @Override
    public TradingAgent createAgent(String type, String style, Trader t, StockExchange e, NewsBoard n) {
        if (type.equalsIgnoreCase("Institutional")) {
            return institutionalFactory.createAgent(t, e, n, style);
        } else if (type.equalsIgnoreCase("Retail")) {
            return retailFactory.createAgent(t, e, n, style);
        } else {
            throw new IllegalArgumentException("Unknown agent type: " + type);
        }
    }
}