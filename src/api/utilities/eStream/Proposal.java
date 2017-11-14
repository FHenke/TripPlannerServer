package api.utilities.eStream;

public class Proposal {
	
	private String Currency;
	private double TotalFareAmout;
	private Leg[] Legs;
	/**
	 * 
	 */
	public Proposal() {
		super();
	}
	

	/**
	 * @param currency
	 * @param totalFareAmout
	 * @param legs
	 */
	public Proposal(String currency, double totalFareAmout, Leg[] legs) {
		super();
		Currency = currency;
		TotalFareAmout = totalFareAmout;
		Legs = legs;
	}
	/**
	 * @return the currency
	 */
	public String getCurrency() {
		return Currency;
	}
	/**
	 * @return the totalFareAmout
	 */
	public double getTotalFareAmout() {
		return TotalFareAmout;
	}
	/**
	 * @return the legs
	 */
	public Leg[] getLegs() {
		return Legs;
	}

	/**
	 * @param currency the currency to set
	 */
	public void setCurrency(String currency) {
		Currency = currency;
	}
	/**
	 * @param totalFareAmout the totalFareAmout to set
	 */
	public void setTotalFareAmout(double totalFareAmout) {
		TotalFareAmout = totalFareAmout;
	}
	/**
	 * @param legs the legs to set
	 */
	public void setLegs(Leg[] legs) {
		Legs = legs;
	}
	
	

}
