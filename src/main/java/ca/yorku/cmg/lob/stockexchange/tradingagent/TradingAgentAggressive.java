package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

/**
 * A trading agent kind that reacts more eagerly to news.
 */
public class TradingAgentAggressive extends TradingAgent {

	public TradingAgentAggressive(Trader t, StockExchange e, NewsBoard n) {
		super(t, e, n, new AggressiveTradingStrategy());
	}




}
