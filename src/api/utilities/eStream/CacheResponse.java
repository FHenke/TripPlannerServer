package api.utilities.eStream;

public class CacheResponse {

	private String status;
	private boolean dataAvailable;
	private EncodedData data;
	private String errorMessage;
	
	
	/**
	 * 
	 */
	public CacheResponse() {
		super();
	}
	
	/**
	 * @param status
	 * @param dataAvailable
	 * @param data
	 */
	public CacheResponse(String status, boolean dataAvailable, EncodedData data) {
		super();
		this.status = status;
		this.dataAvailable = dataAvailable;
		this.data = data;
	}
	
	
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @return the dataAvailable
	 */
	public boolean isDataAvailable() {
		return dataAvailable;
	}
	/**
	 * @return the data
	 */
	public EncodedData getData() {
		return data;
	}
	
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @param dataAvailable the dataAvailable to set
	 */
	public void setDataAvailable(boolean dataAvailable) {
		this.dataAvailable = dataAvailable;
	}
	/**
	 * @param data the data to set
	 */
	public void setData(EncodedData data) {
		this.data = data;
	}
	
	
}
