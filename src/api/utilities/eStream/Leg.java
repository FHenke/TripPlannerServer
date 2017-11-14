package api.utilities.eStream;

public class Leg {

	private Segment[] Segments;

	/**
	 * 
	 */
	public Leg() {
		super();
	}

	/**
	 * @param segments
	 */
	public Leg(Segment[] segments) {
		super();
		Segments = segments;
	}

	/**
	 * @return the segments
	 */
	public Segment[] getSegments() {
		return Segments;
	}

	/**
	 * @param segments the segments to set
	 */
	public void setSegments(Segment[] segments) {
		Segments = segments;
	}
	
	
	
}