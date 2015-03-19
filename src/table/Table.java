package table;

import java.util.Hashtable;
import page.PageController;
import exceptions.DBAppException;

public class Table {
	private PageController page;
	private String tableName;

	public Table(String name) {
		this.tableName = name;
		this.page = new PageController();
	}

	public PageController getPage() {
		return page;
	}

	public String getTableName() {
		return tableName;
	}

}
