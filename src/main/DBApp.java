package main;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;

import exceptions.DBAppException;
import exceptions.DBEngineException;

public class DBApp implements DBMainInterface{

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DBApp app = new DBApp();
		app.init();
	}
	
	private void createFolder(String name) {
		File pagesDirectory = new File(name);
		if (!pagesDirectory.exists()) {
			try {
				pagesDirectory.mkdir();
			} catch (SecurityException se) {
				se.printStackTrace();
			}
		}
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		this.createFolder("pages");
		this.createFolder("indexes");
	}

	@Override
	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws DBAppException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createIndex(String strTableName, String strColName)
			throws DBAppException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createMultiDimIndex(String strTableName,
			Hashtable<String, String> htblColNames) throws DBAppException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertIntoTable(String strTableName,
			Hashtable<String, String> htblColNameValue) throws DBAppException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFromTable(String strTableName,
			Hashtable<String, String> htblColNameValue, String strOperator)
			throws DBEngineException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Iterator selectFromTable(String strTable,
			Hashtable<String, String> htblColNameValue, String strOperator)
			throws DBEngineException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveAll() throws DBEngineException {
		// TODO Auto-generated method stub
		
	}

}
