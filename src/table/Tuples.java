package table;

public class Tuples {
	private int index ,page;
	private String key;
	
	public Tuples(int page, int index, String Key){
		this.index=index;
		this.page = page;
		this.key=key;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
