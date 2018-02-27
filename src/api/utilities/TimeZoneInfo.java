package api.utilities;


public class TimeZoneInfo {

	
	long dstOffset = Long.MAX_VALUE;
	long rawOffset = Long.MAX_VALUE;
	String timeZoneId = "";
	String timeZoneName = "";
	
	/**
	 * @return the dstOffset
	 */
	public long getDstOffset() {
		return dstOffset;
	}
	/**
	 * @return the rawOffset
	 */
	public long getRawOffset() {
		return rawOffset;
	}
	/**
	 * @param dstOffset the dstOffset to set
	 */
	public void setDstOffset(long dstOffset) {
		this.dstOffset = dstOffset;
	}
	/**
	 * @param rawOffset the rawOffset to set
	 */
	public void setRawOffset(long rawOffset) {
		this.rawOffset = rawOffset;
	}
	/**
	 * @return the timeZoneId
	 */
	public String getTimeZoneId() {
		return timeZoneId;
	}
	/**
	 * @return the timeZoneName
	 */
	public String getTimeZoneName() {
		return timeZoneName;
	}
	/**
	 * @param timeZoneId the timeZoneId to set
	 */
	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}
	/**
	 * @param timeZoneName the timeZoneName to set
	 */
	public void setTimeZoneName(String timeZoneName) {
		this.timeZoneName = timeZoneName;
	}
	
	
	
	
}
