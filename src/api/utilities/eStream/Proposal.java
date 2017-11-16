package api.utilities.eStream;

public class Proposal {
	
	private String Currency;
	private double TotalFareAmount;
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
	public Proposal(String currency, double totalFareAmount, Leg[] legs) {
		super();
		Currency = currency;
		TotalFareAmount = totalFareAmount;
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
	public double getTotalFareAmount() {
		return TotalFareAmount;
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
	public void setTotalFareAmount(double totalFareAmount) {
		TotalFareAmount = totalFareAmount;
	}
	/**
	 * @param legs the legs to set
	 */
	public void setLegs(Leg[] legs) {
		Legs = legs;
	}
	
	

}
