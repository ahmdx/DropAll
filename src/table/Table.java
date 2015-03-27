package table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import page.PageController;
import exceptions.DBAppException;

public class Table implements Serializable {
	private static final long serialVersionUID = 1L;
	private PageController controller;
	private String tableName;
	private Hashtable<String, String> colTypes;
	private Hashtable<String, String> colRefs;
	private ArrayList<String> colPK;
	private Hashtable<String, String> colSingleIndexName;
	private ArrayList<IndexTuple> multiIndex;
	

	public Table(String name, Hashtable<String, String> t,
			Hashtable<String, String> r) {

		this.tableName = name;
		this.controller = new PageController();
		this.colTypes = t;
		this.colRefs = r;
		this.colPK = new ArrayList<String>();
		this.colSingleIndexName = new Hashtable<String, String>();
		this.multiIndex= new ArrayList<IndexTuple>();
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
	
	
	public ArrayList<String> getColPK() {
		return colPK;
	}

	public void setColPK(ArrayList<String> colPK) {
		this.colPK = colPK;
	}

	public Hashtable<String, String> getColSingleIndexName() {
		return colSingleIndexName;
	}

	public void setColSingleIndexName(Hashtable<String, String> colSingleIndexName) {
		this.colSingleIndexName = colSingleIndexName;
	}

	public ArrayList<IndexTuple> getMultiIndex() {
		return multiIndex;
	}

	public void setMultiIndex(ArrayList<IndexTuple> multiIndex) {
		this.multiIndex = multiIndex;
	}


}
