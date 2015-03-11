package table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
					throws DBAppException {
		File csvFile = new File("metafile.csv");
		PrintWriter writer;
		try {
			writer = new PrintWriter(csvFile);

			String[] hash;
			String[] foreignKey;
			String[] tableColumns = htblColNameType.toString().split(",");
			String[] foreignColumns = htblColNameRefs.toString().split(",");

			for (int i = 0; i < formatList.length; i++) {
				if (i == formatList.length - 1) {
					writer.print(formatList[i]);
				} else {
					writer.print(formatList[i].trim() + ","+" ");
				}
			}
			writer.println();

			for (int i = 0; i < tableColumns.length; i++) {
				hash = tableColumns[i].split("=");
				writer.print(strTableName + "," + " "); // writing table name
				writer.print(keyHelper(strKeyColName, nameHelper(hash[0].trim()))); // writing if column is PK
				writer.print(nameHelper(hash[0].trim()) + "," + " "); // writing column name
				writer.print(typeHelper(hash[1].trim())); // writing column type
				//writer.print(fkHelper(htblColNameRefs.containsKey(hash[0]),hash[0], i));
				writer.println();
			}
			writer.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

	}

	private String typeHelper(String x) {
		char firstChar = x.toLowerCase().charAt(0);
		switch (firstChar) {
		case 'i':
			return "java.lang.Integer, ";
		case 'd':
			return "java.util.Date, ";
		case 'b':
			return "java.lang.Boolean, ";
		case 's':
		case 'v':
			return "java.lang.String, ";
		default:
			return "error";
		}
	}

	private String nameHelper(String x) { // adjusts the name key in a hashtable
		if(x.charAt(0)=='{'){
			return x.substring(1);
		} else {
			return x;
		}
	}

	private String keyHelper(String key, String current) {
		if (key.trim()==current.trim()) {
			return "True, ";
		} else {
			return "False, ";
		}
	}

	/*	private String fkHelper(String key, String current) {
		if (key==current) {
			return nameHelper(key, index);
		} else {
			return "null";
		}
	}*/

	public static void main(String[] args) throws DBAppException {
		// TODO Auto-generated method stub
		Tables t = new Tables();
		Hashtable<String, String> cols = new Hashtable<String, String>();
		cols.put("ID", "int");
		cols.put("name", "string");
		cols.put("DOB", "date");
		Hashtable<String, String> refs = new Hashtable<String, String>();

		t.createTable("shit", cols, refs, "ID");

	}

}
