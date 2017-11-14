package api.utilities.eStream;

public class ResultSet {
	private String message;
	private Proposal[] proposals;

	/**
	 * 
	 */
	public ResultSet() {
		super();
	}
	
	public ResultSet(String message){
		this.message = message;
	}

	/**
	 * @param proposals
	 */
	public ResultSet(Proposal[] proposals) {
		super();
		this.proposals = proposals;
	}
	
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the proposals
	 */
	public Proposal[] getProposals() {
		return proposals;
	}

	/**
	 * @param proposals the proposals to set
	 */
	public void setProposals(Proposal[] proposals) {
		this.proposals = proposals;
	}
	
	
	
	
}
