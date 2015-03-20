package table;

import java.io.Serializable;
import java.util.Hashtable;
import page.PageController;
import exceptions.DBAppException;

public class Table implements Serializable {
	private PageController controller;
	private String tableName;
	private Hashtable<String, String> colTypes;
	private Hashtable<String, String> colRefs;

	public Table(String name, Hashtable<String, String> t,
			Hashtable<String, String> r) {

		this.tableName = name;
		this.controller = new PageController();
		this.colTypes = t;
		this.colRefs = r;
	}

	public PageController getController() {
		return controller;
	}

	public String getTableName() {
		return tableName;
	}

	public Hashtable<String, String> getColTypes() {
		return colTypes;
	}

	public void setColTypes(Hashtable<String, String> colTypes) {
		this.colTypes = colTypes;
	}

	public Hashtable<String, String> getColRefs() {
		return colRefs;
	}

	public void setColRefs(Hashtable<String, String> colRefs) {
		this.colRefs = colRefs;
	}
}
