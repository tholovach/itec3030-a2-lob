package ca.yorku.cmg.lob.stockexchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ca.yorku.cmg.lob.orderbook.Ask;
import ca.yorku.cmg.lob.orderbook.Bid;
import ca.yorku.cmg.lob.orderbook.Orderbook;
import ca.yorku.cmg.lob.orderbook.Trade;
import ca.yorku.cmg.lob.security.Security;
import ca.yorku.cmg.lob.security.SecurityList;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.stockexchange.tradingagent.TradingAgent;
import ca.yorku.cmg.lob.stockexchange.tradingagent.TradingAgentFactory;
import ca.yorku.cmg.lob.trader.Trader;
import ca.yorku.cmg.lob.trader.TraderInstitutional;
import ca.yorku.cmg.lob.trader.TraderRetail;
import ca.yorku.cmg.lob.tradestandards.IOrder;

/**
 * Represents a stock exchange that manages securities, accounts, orders, and trades.
 */
public class StockExchange {

		private Orderbook book;
		private NewsBoard newsDesk;
		
		private SecurityList securities = new SecurityList();
		private AccountsList accounts = new AccountsList();
		private ArrayList<Trade> tradesLog = new ArrayList<Trade>();
		private ArrayList<TradingAgent> traders = new ArrayList<TradingAgent>();
		
		private ArrayList<IOrder> log = new ArrayList<>();
		
		private Map<String, Integer> prices = new HashMap<String, Integer>();
					
		long totalFees = 0;

		/**
		 * A <a href="https://en.wikipedia.org/wiki/Method_stub">method stub</a> that traders or other calling environments can call to register a new order. <p>NOTE: the order is not processed by the stock exchange, feature yet to be implemented. Orders are kept in a list for testing.</p> 
		 * @param order The {@linkplain IOrder} implementing object to be submitted. 
		 * @param time The time of submission. 
		 */
		public void submitOrder(IOrder order, long time) {
			if (order instanceof Bid) {
				book.getBids().addOrder((Bid) order);
			} else {
				book.getAsks().addOrder((Ask) order);
			}
			log.add(order);
		}

		/**
		 * A <a href="https://en.wikipedia.org/wiki/Method_stub">method stub</a> that returns the price of a ticker. <p>NOTE: the price is based on an initial list read from a file. Price identification based on consulting the Ask halfbook not implemented</p> 
		 * @param tkr The ticker whose price is to be returned
		 */
		public int getPrice(String tkr) {
			return(prices.get(tkr));
		}
		
		
		/**
		 * Generate a string that describes the output, for testing purposes.  
		 * @return The output
		 */
		public String getLogTestSample() {
			String out = "";
			for (IOrder r: log) {
				out += String.format("[%3d  %s  %6d  %6d]", 
						r.getTrader().getID(), r.getSecurity().getTicker(), r.getPrice(), r.getQuantity());
			}
			return(out);
		}
		
		
	    /**
	     * Default constructor for the Exchange class.
	     */
		public StockExchange(){
			book = new Orderbook();
			newsDesk = new NewsBoard(getSecurities());
		}

		/**
		 * Read the initial prices of the stocks from a file. Format: [Ticker, Company Title, Price]. <p>NOTE: Quick fix to be used by stubs. Price should be inferred from the Ask book.</p>. 
		 * @param filePath The path of the file
		 */
		public void readPriceListfromFile(String filePath) {
			String line;
			String delimiter = ",";

			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
				while ((line = br.readLine()) != null) {
					String[] parts = line.split(delimiter);

					// Ensure there are exactly 3 columns
					if (parts.length != 3) {
						System.err.println("Invalid line format: " + line);
						continue;
					}

					String tkr = parts[0].trim();
					String priceStr = parts[2].trim();

					try {
						int value = Integer.parseInt(priceStr); // Convert the value to an integer
						prices.put(tkr, value);
					} catch (NumberFormatException e) {
						System.err.println("Invalid number format for value: " + priceStr + " in line: " + line);
					}
				}
			} catch (IOException e) {
				System.err.println("Error reading the file: " + e.getMessage());
			}

		}

		
	    /**
	     * Reads the security list from a file and populates the exchange.
	     * 
	     * @param path the path to the security list file
	     */
		public void readSecurityListfromFile(String path) {
		    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
	            String line;
	            boolean isFirstLine = true; // Skip header

	            while ((line = br.readLine()) != null) {
	                if (isFirstLine) {
	                    isFirstLine = false;
	                    continue;
	                }
	                String[] parts = line.split(",", -1); // Split by comma
	                if (parts.length >= 2) {
	                    String code = parts[0].trim();
	                    String description = parts[1].trim();
	                    securities.addSecurity(code, description);
	                } else {
	                    System.err.println("Skipping malformed line: " + line);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
	    /**
	     * Reads the accounts list from a file and populates the exchange.
	     * 
	     * @param path the path to the accounts list file
	     */
		public void readAccountsListFromFile(String path) {
		    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
	            String line;
	            boolean isFirstLine = true; // Skip header

	            while ((line = br.readLine()) != null) {
	                if (isFirstLine) {
	                    isFirstLine = false;
	                    continue;
	                }
	                String[] parts = line.split(",", -1); // Split by comma
	                if (parts.length >= 5) {
	                    String traderTitle = parts[0].trim();
	                    String traderType = parts[1].trim();
	                    String accType = parts[2].trim();
	                    long initBalance = Long.parseLong(parts[3].trim());
	                    String tradingStyle = parts[4].trim();
	                	Trader t;
	                    if (traderType.equals("Retail")) {
	                    	t = new TraderRetail(traderTitle);
	                    } else {
	                    	t = new TraderInstitutional(traderTitle);
	                    }
	                    if (accType.equals("Basic")) {
	                    	accounts.addAccount(new AccountBasic(t,initBalance));
	                    } else {
	                    	accounts.addAccount(new AccountPro(t,initBalance));
	                    }
	                    // Use factory to create trading agents
	                    TradingAgentFactory factory = new TradingAgentFactory();
	                    TradingAgent agent = factory.createAgent(traderType, tradingStyle, t, this, newsDesk);
	                    traders.add(agent);
	                    
	                } else {
	                    System.err.println("Skipping malformed line (two few attributes): " + line);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
	    /**
	     * Reads initial positions from a file and updates account holdings.
	     * 
	     * @param path the path to the initial positions file
	     */
		public void readInitialPositionsFromFile(String path) {
		    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
	            String line;
	            boolean isFirstLine = true; // Skip header

	            while ((line = br.readLine()) != null) {
	                if (isFirstLine) {
	                    isFirstLine = false;
	                    continue;
	                }
	                String[] parts = line.split(",", -1); // Split by comma
	                if (parts.length >= 3) {
	                    Integer tid = Integer.valueOf(parts[0].trim());
	                    String tkr = parts[1].trim();
	                    Integer count = Integer.valueOf(parts[2].trim());
	                    Trader trad = accounts.getTraderByID(tid); 
	                    //does the trader id have an account? Is the ticker supported?
	                    if (trad == null) {
	                    	System.err.println("Initial Balances: Trader does not exist: " + line);
	                    } else if (securities.getSecurityByTicker(tkr) == null) { 
	                    	System.err.println("Initial Balances: Ticker not traded in this exchange: " + line);
	                    } else {
	                    	accounts.getTraderAccount(trad).updatePosition(tkr, count);
	                    }
	                } else {
	                    System.err.println("Skipping malformed line (too few attributes): " + line);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
	    /**
	     * Processes a file containing orders and submits them to the exchange.
	     * 
	     * @param path the path to the orders file
	     */
		public void processOrderFile(String path) {
		    try (BufferedReader br = new BufferedReader(new FileReader(path))) {
	            String line;
	            boolean isFirstLine = true; // Skip header

	            while ((line = br.readLine()) != null) {
	                if (isFirstLine) {
	                    isFirstLine = false;
	                    continue;
	                }
	                String[] parts = line.split(",", -1); // Split by comma
	                if (parts.length >= 6) {
	                    int traderID = Integer.valueOf(parts[0].trim());
	                    String tkr = parts[1].trim();
	                    String type = parts[2].trim();
	                    int qty = Integer.valueOf(parts[3].trim());
	                    int price = Integer.valueOf(parts[4].trim());
	                    long time = Long.valueOf(parts[5].trim());
	                    
	                    Trader t = getAccounts().getTraderByID(traderID);
	                    Security sec = getSecurities().getSecurityByTicker(tkr); 
	                    
	                    if ((t!=null) && (sec!=null)) {
	                        if (type.equals("ask")) {
	                        	submitOrder(new Ask(t,sec,price,qty,time), time);
	                        } else if (type.equals("bid")) {
	                        	submitOrder(new Bid(t,sec,price,qty,time), time);
	                        } else {
	                        	System.err.println("Order type not found (skipping): " + line);
	                        }
	                    }
	                } else {
	                    System.err.println("Skipping malformed line: " + line);
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		




		/**
	     * Prints a table of current ask orders.
	     * 
	     * @param header whether to include a header in the output
	     * @return a string representation of the ask table
	     */
		public String printAskTable(boolean header) {
			return(book.getAsks().printAllOrders(header));
		}
		
	    /**
	     * Prints a table of current bid orders.
	     * 
	     * @param header whether to include a header in the output
	     * @return a string representation of the bid table
	     */
		public String printBidTable(boolean header) {
			return(book.getBids().printAllOrders(header));
		}
		
	    /**
	     * Prints a log of completed trades.
	     * 
	     * @param header whether to include a header in the output
	     * @return a string representation of the trades log
	     */
		public String printTradesLog(boolean header) {
			String output = "";
			if (header) {
				output = "[From____  To______  Tkr_  Quantity  Price__  Time____]\n";
				//"[%8d  %8d  %s  %8d  %7.2f  %8d]\n", 
			}
			for (Trade t: tradesLog) {
				output += t.toString();
			}
			return (output);
		}

	    /**
	     * Prints account balances of the exchange's customers
	     * 
	     * @param header whether to include a header in the output
	     * @return a string representation of account balances
	     */
		public String printBalances(boolean header) {
			return(accounts.debugPrintBalances(header));
		}
		
	    /**
	     * Prints the total fees collected by the exchange.
	     * 
	     * @param header whether to include a header in the output
	     * @return a string representation of fees collected
	     */
		public String printFeesCollected(boolean header) {
			if (header) {
				return (String.format("            Fees Collected TOTAL: %16s", 
						String.format("$%,.2f",this.totalFees/100.0)));
			} else {
				return (String.format("%16s", 
						String.format("$%,.2f",this.totalFees/100.0)));
			}
		}
		
		
		
		//
		// G E T T E R S
		//
		
	    /**
	     * Retrieves the list of securities managed by the exchange.
	     * 
	     * @return the {@linkplain ca.yorku.cmg.lob.security.SecurityList} object
	     */
		public SecurityList getSecurities() {
			return securities;
		}
		
	    /**
	     * Retrieves the list of accounts managed by the exchange.
	     * 
	     * @return the {@linkplain ca.yorku.cmg.lob.exchange.AccountsList} object
	     */
		public AccountsList getAccounts() {
			return accounts;
		}
				
		/**
		 * Returns the list of {@linkplain TradingAgent} objects associated with this exchange.
		 * @return The list of {@linkplain TradingAgent} objects associated with this exchange.
		 */
		public ArrayList<TradingAgent> getTraders() {
			return traders;
		}

		/**
		 * Returns the {@linkplain NewsBoard} object associated with this exchange.
		 * @return the {@linkplain NewsBoard} object associated with this exchange.
		 */
		public NewsBoard getNewsBoard() {
			return newsDesk;
		}

		
	}
