/**
 * 
 */
package utilities;

import api.utilities.eStream.Leg;

/**
 * @author Florian
 *
 */
public class CarrierList {
	private CarrierList nextCarrier = null; 
	private String carrierName = null;
	private String url = null;
	
	/**
	 * Constractor with carrier name
	 * @param carrierName Name of Carrier
	 */
	public CarrierList(String carrierName){
		this.carrierName = carrierName;
	}
	
	public CarrierList(String carrierName, String url){
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
	 * @return the nextCarrier
	 */
	public CarrierList getNextCarrier() {
		return nextCarrier;
	}

	/**
	 * @param nextCarrier the nextCarrier to set
	 */
	public void setNextCarrier(CarrierList nextCarrier) {
		this.nextCarrier = nextCarrier;
	}

	/**
	 * Adds a new Carrier in the end of the CarrierList
	 * @param carrierName the Carrier Name to set
	 */
	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}
	
	/**
	 * Adds a new Carrier in the end of the CarrierList
	 * @param carrier the Carrier to add to the List
	 */
	public void addCarrier(CarrierList carrier){
		if(nextCarrier == null)
			nextCarrier = carrier;
		else
			nextCarrier.addCarrier(carrier);
	}
	
}
