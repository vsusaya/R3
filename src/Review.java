
public class Review {
	
	private String review;
	private int queryNum;
	
	public Review(String review, int queryNum) {
		this.review = review;
		this.queryNum = queryNum;
	}

	protected String getReview() {
		return review;
	}
	
	protected int getQueryNum() {
		return queryNum;
	}
}
