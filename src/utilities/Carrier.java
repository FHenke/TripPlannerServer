package utilities;

public class Carrier {
	private String carrierName = null;
	private String url = null;
	
	/**
	 * Constractor with carrier name
	 * @param carrierName Name of Carrier
	 */
	public Carrier(String carrierName){
		this.carrierName = carrierName;
	}
	
	public Carrier(String carrierName, String url){
		this.carrierName = carrierName;
		this.url = url;
	}

	/**
	 * @return Name of the Carrier
	 */
	public String getCarrierName() {
		return carrierName;
	}

	/**
	 * Adds a new Carrier in the end of the CarrierList
	 * @param carrierName the Carrier Name to set
	 */
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	
}
