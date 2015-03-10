package table;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;

import exceptions.DBAppException;

public class Tables {
	private String format = "Table Name, Column Name, Column Type, Key, Indexed, ReferencesTable, ReferencesColumn";
	private String[] formatList = format.split(",");

	public Tables() {

	}

	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws DBAppException, FileNotFoundException {
		File csvFile = new File("metaFile.csv");
		try(FileInputStream reader = new FileInputStream(csvFile))
		;
		
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
