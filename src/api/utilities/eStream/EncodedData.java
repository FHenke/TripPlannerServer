package api.utilities.eStream;

public class EncodedData {

	private String lastModified;
	private long recordAgeInMs;
	private String base64GzippedResponse;
	
	
	
	
	
	/**
	 * 
	 */
	public EncodedData() {
		super();
	}
	
	
	/**
	 * @param lastModified
	 * @param recordAgeInMs
	 * @param base64GzippedResponse
	 */
	public EncodedData(String lastModified, long recordAgeInMs, String base64GzippedResponse) {
		super();
		this.lastModified = lastModified;
		this.recordAgeInMs = recordAgeInMs;
		this.base64GzippedResponse = base64GzippedResponse;
	}
	
	
	/**
	 * @return the lastModified
	 */
	public String getLastModified() {
		return lastModified;
	}
	/**
	 * @return the recordAgeInMs
	 */
	public long getRecordAgeInMs() {
		return recordAgeInMs;
	}
	/**
	 * @return the base64GzippedResponse
	 */
	public String getBase64GzippedResponse() {
		return base64GzippedResponse;
	}
	/**
	 * @param lastModified the lastModified to set
	 */
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	/**
	 * @param recordAgeInMs the recordAgeInMs to set
	 */
	public void setRecordAgeInMs(long recordAgeInMs) {
		this.recordAgeInMs = recordAgeInMs;
	}
	/**
	 * @param base64GzippedResponse the base64GzippedResponse to set
	 */
	public void setBase64GzippedResponse(String base64GzippedResponse) {
		this.base64GzippedResponse = base64GzippedResponse;
	}
	
	
	
}
