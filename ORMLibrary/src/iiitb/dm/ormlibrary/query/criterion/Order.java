package iiitb.dm.ormlibrary.query.criterion;

public class Order {


	private boolean ascending;
	private String propertyName;
	
	public Order(String propertyName, boolean ascending) {
		
		this.ascending = ascending;
		this.propertyName = propertyName;
	}
	
	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	

	public static Order asc(String propertyName) {
		return new Order(propertyName, true);
	}

	public static Order desc(String propertyName) {
		return new Order(propertyName, false);
	}
}
