package table;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.DBAppException;

public class Tables {
	private String format = "Table Name, Primary Key, Column Name, Column Type, Indexed, References";
	private String[] formatList = format.split(",");
	private Hashtable<String, String> tableColumns;
	private Hashtable<String, String> tableReferences;
	
	public Tables() {

	}

	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
					throws DBAppException {
		
		tableColumns = htblColNameType;
		tableReferences = htblColNameRefs;

		File csvFile = new File("metafile.csv");
		PrintWriter writer;
		try {
			writer = new PrintWriter(csvFile);

			String[] hash;
			String error = referencesTableFormat(htblColNameRefs);
			String[] tableColumns = htblColNameType.toString().split(",");

			if (error != null) {
				System.err.println("Error in referencing another table");
				System.err
						.println("Please specify the column name you want to refer to in: "
								+ nameHelper(error));
				System.err
						.println("To specifiy column place a \".\" after the table name");
				return;
			}

			for (int i = 0; i < formatList.length; i++) {
				if (i == formatList.length - 1) {
					writer.print(formatList[i]);
				} else {
					writer.print(formatList[i].trim() + "," + " ");
				}
			}
			writer.println();

			for (int i = 0; i < tableColumns.length; i++) {
				hash = tableColumns[i].split("=");
				writer.print(strTableName + "," + " "); // writing table name
				writer.print(keyHelper(strKeyColName, nameHelper(hash[0]))); // writing
																				// if
																				// column
																				// is
																				// PK
				writer.print(nameHelper(hash[0].trim()) + "," + " "); // writing
																		// column
																		// name

				if (typeHelper(hash[1].trim()) == hash[1].trim()) {
					System.err.println("unkown data type in: " + hash[0].trim()
							+ " ==> " + hash[1].trim());
					return;
				}

				writer.print(typeHelper(hash[1].trim())); // writing column type
				writer.print("False, "); //writing index
				writer.print(fkHelper(htblColNameRefs, nameHelper(hash[0]))); //writing references
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
		case 'i': // integer
			return "java.lang.Integer, ";
		case 'd': // date
			return "java.util.Date, ";
		case 'b': // boolean
			return "java.lang.Boolean, ";
		case 's': // string
		case 'v': // varchar
			return "java.lang.String, ";
		default: // not in the list
			return x;
		}
	}

	private String nameHelper(String x) { // adjusts the name key in a hashtable
		if (x.startsWith("{") && x.endsWith("}")) {
			return x.substring(1, x.length() - 1);
		} else {
			if (x.startsWith("{")) {
				return x.substring(1);
			} else {
				if (x.endsWith("}")) {
					return x.substring(0, x.length() - 1);
				} else {
					return x;
				}

			}
		}
	}

	private String keyHelper(String key, String current) {
		if (key.trim().equals(current.trim())) {
			return "True, ";
		} else {
			return "False, ";
		}
	}

	private String fkHelper(Hashtable<String, String> refsTable, String current) {
		if (refsTable.containsKey(current.trim())) {
			return refsTable.get(current.trim());
		} else {
			return "null";
		}
	}

	private String referencesTableFormat(Hashtable<String, String> x) {
		String[] hash;
		String[] foreignColumns = x.toString().split(",");

		String pattern = "(\\w)(\\.)(\\w)";
		Pattern regexp = Pattern.compile(pattern);
		Matcher checker;
		if (x.isEmpty()) {
			return "empty";
		}

		for (int i = 0; i < foreignColumns.length; i++) {
			hash = foreignColumns[i].split("=");
			checker = regexp.matcher(hash[1]);
			if (!checker.find()) {
				return hash[1];
			}
		}
		return null;
	}

	public static void main(String[] args) throws DBAppException {
		// TODO Auto-generated method stub
		Tables t = new Tables();
		Hashtable<String, String> cols = new Hashtable<String, String>();
		cols.put("ID", "int");
		cols.put("name", "date");
		cols.put("DOB", "date");
		Hashtable<String, String> refs = new Hashtable<String, String>();
		refs.put("name", "user.fname");
		refs.put("ID", "employee.ID");
		t.createTable("demo", cols, refs, "name");

	}

	public Hashtable<String, String> getTableColumns() {
		return tableColumns;
	}


	public Hashtable<String, String> getTableReferences() {
		return tableReferences;
	}


}
