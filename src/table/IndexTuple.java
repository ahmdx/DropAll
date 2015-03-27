package table;

import java.util.ArrayList;

public class IndexTuple {
	private ArrayList<String> cols;
	private String page;
	
	public IndexTuple(ArrayList<String> cols, String page) {
		this.cols = cols;
		this.page = page;
		
	}

	public ArrayList<String> getCols() {
		return cols;
	}

	public void setCols(ArrayList<String> cols) {
		this.cols = cols;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

}
