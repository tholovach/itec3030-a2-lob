package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

/**
 * A trading agent kind that reacts more carefully and conservatively to news.
 */
public class TradingAgentConservative extends TradingAgent {

	public TradingAgentConservative(Trader t, StockExchange e, NewsBoard n) {
		super(t, e, n, new ConservativeTradingStrategy());
	}



}
