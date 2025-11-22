package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.Event;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

/**
 * An trading agent that receives news and reacts by submitting ask or bid orders.
 */
public abstract class TradingAgent implements INewsObserver {
	protected Trader t;
	protected StockExchange exc;
	protected NewsBoard news;
	
	/**
	 * Constructor
	 * @param t The {@linkplain Trader} object associated with the agent.
	 * @param e The {@linkplain StockExchange} object at which the agent has an account and trades in. 
	 * @param n The {@linkplain NewsBoard} object that generates news events.
	 */
	public TradingAgent(Trader t, StockExchange e, NewsBoard n) {
		this.t=t;
		this.exc = e;
		this.news = n;
		// Register this agent as an observer with the NewsBoard
		this.news.addObserver(this);
	}
	
	/**
	 * Method to be called as time advances to {@code time}. In response the TradingAgent will poll the NewsBoard for events.
	 * @param time The time to advance to.
	 */
	public void timeAdvancedTo(long time) {
		pollForEvents(time);
	}

	/**
	 * Examine if an event is relevant for the Agent, i.e., if the Agent has a position on it.
	 * @param e The {@linkplain Event} object in question
	 */
	private void examineEvent(Event e) {
		int positionInSecurity = exc.getAccounts().getTraderAccount(t).getPosition(e.getSecrity().getTicker());
		if (positionInSecurity > 0) {
			actOnEvent(e,positionInSecurity,exc.getPrice(e.getSecrity().getTicker()));
		}
	}

	
	/**
	 * Check into the {@linkplain NewsBoard} if there are any events at time {@code time}. If there is one (it assumes only one event at a time), send it for examination.
	 * @param time The time for which to poll for events. Unit is days.
	 */
	private void pollForEvents(long time) {
		Event e = news.getEventAt(time);
		if (e!=null) {
			examineEvent(e);
		}

	}
	
	
	/**
	 * Act in response to a news {@linkplain Event}. Exact reaction strategy to be implemented by specialized agents.
	 * @param e The {@linkplain Event} in question
	 * @param pos The position (number of units) of the trader to the ticker that is mentioned in the Event.
	 * @param price The current price of the relevant ticker. 
	 */
	protected abstract void actOnEvent(Event e, int pos, int price);
	
	/**
	 * Implementation of INewsObserver.update() - called when NewsBoard pushes an event
	 */
	@Override
	public void update(Event e) {
		examineEvent(e);
	}
	
	

}
