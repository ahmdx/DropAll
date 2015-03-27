package DropAll;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import table.TablesController;
import exceptions.DBAppException;
import exceptions.DBEngineException;

public class DBApp implements DBMainInterface {
	private TablesController tablesController;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DBApp app = new DBApp();
		app.getPropValues();
//		app.init();
	}

	private void createDirectory(String name) {
		File directory = new File(name);
		if (!directory.exists()) {
			try {
				directory.mkdir();
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		this.createDirectory("data");
		this.createDirectory("data/pages");
		this.createDirectory("data/indexes");

		this.tablesController = TablesController.load();
		this.tablesController = (this.tablesController == null) ? new TablesController()
				: this.tablesController;
	}

	@Override
	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws DBAppException {
		// TODO Auto-generated method stub
		this.tablesController.createTable(strTableName, htblColNameType, htblColNameRefs, strKeyColName);
	}

	@Override
	public void createIndex(String strTableName, String strColName)
			throws DBAppException {
		// TODO Auto-generated method stub
		this.tablesController.createIndex(strTableName, strColName);
	}

	@Override
	public void createMultiDimIndex(String strTableName,
			Hashtable<String, String> htblColNames) throws DBAppException {
		// TODO Auto-generated method stub
//		this.tablesController.crea
	}

	@Override
	public void insertIntoTable(String strTableName,
			Hashtable<String, String> htblColNameValue) throws DBAppException {
		// TODO Auto-generated method stub
		this.tablesController.insertIntoTable(strTableName, htblColNameValue);
	}

	@Override
	public void deleteFromTable(String strTableName,
			Hashtable<String, String> htblColNameValue, String strOperator)
			throws DBEngineException {
		// TODO Auto-generated method stub
		this.deleteFromTable(strTableName, htblColNameValue, strOperator);
	}

	@Override
	public Iterator selectFromTable(String strTable,
			Hashtable<String, String> htblColNameValue, String strOperator)
			throws DBEngineException {
		// TODO Auto-generated method stub
		return this.tablesController.selectFromTable(strTable, htblColNameValue, strOperator);
//		return null;
	}

	@Override
	public void saveAll() throws DBEngineException {
		// TODO Auto-generated method stub
		this.tablesController.save();
	}
	
	public String getPropValues() throws Exception {
		Properties prop = new Properties();
		String propFileName = "config/DBApp.properties";
 
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}
		String maxRows = prop.getProperty("MaximumRowsCountinPage");
		return maxRows;
	}

}
