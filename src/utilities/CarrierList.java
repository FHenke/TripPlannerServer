/**
 * 
 */
package utilities;

/**
 * @author Florian
 *
 */
public class CarrierList {
	private CarrierList nextCarrier; 
	private String carrierName;
	
	/**
	 * Empty constructor
	 */
	public CarrierList(){
		
	}
	
	/**
	 * Constractor with carrier name
	 * @param carrierName Name of Carrier
	 */
	public CarrierList(String carrierName){
		this.carrierName = carrierName;
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
