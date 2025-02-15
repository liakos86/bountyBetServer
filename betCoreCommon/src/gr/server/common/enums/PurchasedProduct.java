package gr.server.common.enums;

public enum PurchasedProduct {
	
	TOPUP_1000("topup1000", 1000),
	
	TOPUP_3000("topup3000", 3000);

	private String productName;

	private int credits;
	
	PurchasedProduct(String productName, int credits) {
		this.productName = productName;
		this.credits = credits;
	}
	
	public static PurchasedProduct fromName(String name) {
		for(PurchasedProduct pp : PurchasedProduct.values()) {
			if (pp.getProductName().equals(name)) {
				return pp;
			}
		}
		
		throw new RuntimeException("Product not found:" + name);
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getCredits() {
		return credits;
	}

	public void setCredits(int credits) {
		this.credits = credits;
	}
	
}
