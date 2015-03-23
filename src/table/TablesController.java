package table;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import page.Page;
import exceptions.DBAppException;
import exceptions.DBEngineException;
import table.Tuples;

public class TablesController implements Serializable {
	private static final long serialVersionUID = 1L;
	private String format = "Table Name, Primary Key, Column Name, Column Type, Indexed, References";
	private String[] formatList = format.split(",");
	private Hashtable<String, String> tableColumns;
	private Hashtable<String, String> tableReferences;
	private ArrayList<Table> allTables = new ArrayList<Table>();
	private Table tableObject;
	private File csvFile;

	public TablesController() {
	}

	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws DBAppException {

		tableColumns = htblColNameType;
		tableReferences = htblColNameRefs;

		PrintWriter writer;
		if (!csvFile.exists()) {
			csvFile = new File("metafile.csv");

			try {
				writer = new PrintWriter(csvFile);

				for (int i = 0; i < formatList.length; i++) {
					if (i == formatList.length - 1) {
						writer.print(formatList[i]);
					} else {
						writer.print(formatList[i].trim() + "," + " ");
					}
				}

				writer.println();
				writeIntoCSVFile(strTableName, htblColNameType,
						htblColNameRefs, strKeyColName, writer);

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

		} else {
			try {
				writer = new PrintWriter(new FileWriter(csvFile, true));
				writeIntoCSVFile(strTableName, htblColNameType,
						htblColNameRefs, strKeyColName, writer);

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

	public void insertIntoTable(String strTableName,
			Hashtable<String, String> htblColNameValue) throws DBAppException {

		int index = searchArraylist(strTableName);
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;
		String[] nameType = this.allTables.get(index).getColTypes().toString()
				.split(",");
		String[] hashTypes;

		if (index == -1) {
			System.err.println("Please ensure that the table name: \""
					+ strTableName + "\" is correct");
			return;
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			hashTypes = nameType[i].split("=");
			if (formatChecker(hashTypes[1], hashValues[1]) != "true") {
				System.err.println(formatChecker(hashTypes[1], hashValues[1]));
				return;
			}
		}

		allTables.get(index).getController().writeToPage(htblColNameValue);
		this.save();
	}

	// bulk insert

	public void insertIntoTable(String strTableName,
			Hashtable<String, String>[] htblColNameValue) throws DBAppException {

		int index = searchArraylist(strTableName);
		String[] keyValue;
		String[] hashValues;
		String[] nameType = this.allTables.get(index).getColTypes().toString()
				.split(",");
		String[] hashTypes;

		if (index == -1) {
			System.err.println("Please ensure that the table name: \""
					+ strTableName + "\" is correct");
			return;
		}

		for (int i = 0; i < htblColNameValue.length; i++) {
			keyValue = htblColNameValue[i].toString().split(",");

			for (int j = 0; j < keyValue.length; j++) {
				hashValues = keyValue[i].split("=");
				hashTypes = nameType[i].split("=");
				if (formatChecker(hashTypes[1], hashValues[1]) != "true") {
					System.err.println(formatChecker(hashTypes[1],
							hashValues[1]));
					return;
				}
			}
		}

		allTables.get(index).getController().writeToPage(htblColNameValue);
		this.save();
	}

	public void deleteFromTable(String strTableName,
			Hashtable<String, String> htblColNameValue, String strOperator)
			throws DBEngineException {
		int index = searchArraylist(strTableName);
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;
		String[] nameType = this.allTables.get(index).getColTypes().toString()
				.split(",");
		String[] hashTypes;

		if (index == -1) {
			System.err.println("Please ensure that the table name: \""
					+ strTableName + "\" is correct");
			return;
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			hashTypes = nameType[i].split("=");
			if (formatChecker(hashTypes[1], hashValues[1]) != "true") {
				System.err.println(formatChecker(hashTypes[1], hashValues[1]));
				return;
			}
		}

		int allPagesCount = this.allTables.get(index).getController()
				.getAllPages().length;
		ArrayList<Tuples> pageIndex = new ArrayList<Tuples>(1);

		if (strOperator == null && htblColNameValue.size() > 1) {
			System.err
					.println("Please choose an operator being either \"AND\" or \"OR\" when having multiple columns");
			return;
		}

		if (htblColNameValue.equals(null) && strOperator == null) {
			this.allTables.get(index).getController().deleteAllPages();
			return;
		}

		if (strOperator.equals("AND")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageANDSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);

			}

			for (int i = 0; i < pageIndex.size(); i++) {
				if (pageIndex.get(i).getKey()
						.equals(keyGenerator(htblColNameValue))) {
					this.allTables
							.get(index)
							.getController()
							.deleteFromPage(pageIndex.get(i).getPage(),
									pageIndex.get(i).getIndex());
				}
			}
		}

		if (strOperator.equals("OR")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageORSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);
			}

			for (int i = 0; i < pageIndex.size(); i++) {
				this.allTables
						.get(index)
						.getController()
						.deleteFromPage(pageIndex.get(i).getPage(),
								pageIndex.get(i).getIndex());
			}
		}
		this.save();
	}

	private void pageANDSearcher(Page page,
			Hashtable<String, String> htblColNameValue, int p,
			ArrayList<Tuples> pageIndex) {
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;
		Tuples t;
		String key;

		for (int j = 0; j < 20; j++) {
			for (int z = 0; z < keyValue.length; z++) {
				hashValues = keyValue[z].split("=");
				if (page.read(j).get(hashValues[0]).equals(hashValues[1])) {

					if (contains(pageIndex, p, j)) {
						int index = arrayListSearcher(pageIndex, p, j);
						key = pageIndex.get(index).getKey();
						pageIndex.get(index).setKey(key + hashValues[0]);
					} else {
						t = new Tuples(p, j, hashValues[0]);
						pageIndex.add(t);
					}
				}

			}
		}
	}

	private void pageORSearcher(Page page,
			Hashtable<String, String> htblColNameValue, int p,
			ArrayList<Tuples> pageIndex) {
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;
		Tuples t;
		String key;

		for (int j = 0; j < 20; j++) {
			for (int z = 0; z < keyValue.length; z++) {
				hashValues = keyValue[z].split("=");
				if (page.read(j).get(hashValues[0]).equals(hashValues[1])) {
					t = new Tuples(p, j, hashValues[0]);
					pageIndex.add(t);
				}
			}

		}
	}

	private String keyGenerator(Hashtable<String, String> t) {
		String[] hashValues = t.toString().split(",");
		String[] hash;
		String concat = "";
		for (int i = 0; i < t.size(); i++) {
			hash = hashValues[i].split("=");
			concat = concat.concat(hash[0]);
		}
		return concat;
	}

	private int arrayListSearcher(ArrayList<Tuples> a, int page, int index) {
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i).getIndex() == index && a.get(i).getPage() == page) {
				return i;
			}
		}
		return -1;
	}

	private boolean contains(ArrayList<Tuples> a, int page, int index) {
		for (int i = 0; i < a.size(); i++) {
			if (a.get(i).getPage() == page && a.get(i).getIndex() == index) {
				return true;
			}
		}
		return false;
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

		String pattern = "^(\\w)(\\.)(\\w)$";
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

	private int searchArraylist(String table) {
		for (int i = 0; i < allTables.size(); i++) {
			if (this.allTables.get(i).getTableName().equals(table)) {
				return i;
			}

		}
		return -1;
	}

	private String formatChecker(String type, String value) {
		char typeChar = type.charAt(10);
		String pattern;
		Pattern regexp;
		Matcher checker;

		switch (typeChar) {
		case 'B':
			if (value.toLowerCase().trim().equals("true")
					|| value.toLowerCase().trim().equals("false")) {
				return "true";
			} else {
				return "Please enter the format of boolean data as being either: \"true\" or \"false\"";
			}

		case 'D':
			pattern = "^(\\d\\d|\\d)(/)(\\d\\d)(/)(\\d\\d\\d\\d)$";
			regexp = Pattern.compile(pattern);
			checker = regexp.matcher(value);
			if (checker.find()) {
				return "true";
			} else {
				return "Please enter the date in the correct format being: \"dd/mm/yyyy\" OR \"d/mm/yyyy\"";
			}

		case 'I':
			pattern = "^\\d+$";
			regexp = Pattern.compile(pattern);
			checker = regexp.matcher(value);
			if (checker.find()) {
				return "true";
			} else {
				return "Please enter only numbers";
			}

		case 'S':
			return "true";
		default:
			return "Unknown data type";

		}
	}

	public final boolean save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("tableController.table")));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static final TablesController load() {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("tableController.table")));
			TablesController tc = (TablesController) ois.readObject();
			ois.close();
			return tc;
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	private void writeIntoCSVFile(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName,
			PrintWriter writer) throws DBAppException {

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
			writer.print("False, "); // writing index
			writer.print(fkHelper(htblColNameRefs, nameHelper(hash[0]))); // writing

			// references
			writer.println();
		}
		writer.close();
		Hashtable<String, String> tmp = new Hashtable<String, String>();
		tmp.put(strKeyColName, "True");

		tableObject = new Table(strTableName, htblColNameType, htblColNameRefs);
		tableObject.setColPK(tmp);
		allTables.add(tableObject);
		this.save();

	}

	public Iterator<?> selectFromTable(String strTable,
			Hashtable<String, String> htblColNameValue, String strOperator)
			throws DBEngineException {

		int index = searchArraylist(strTable);
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;
		String[] nameType = this.allTables.get(index).getColTypes().toString()
				.split(",");
		String[] hashTypes;
		
		if (index == -1) {
			System.err.println("Please ensure that the table name: \""
					+ strTable + "\" is correct");
			return null;
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			hashTypes = nameType[i].split("=");
			if (formatChecker(hashTypes[1], hashValues[1]) != "true") {
				System.err.println(formatChecker(hashTypes[1], hashValues[1]));
				return null;
			}
		}

		int allPagesCount = this.allTables.get(index).getController()
				.getAllPages().length;
		ArrayList<Tuples> pageIndex = new ArrayList<Tuples>(1);
		Iterator iter;
		
		if (strOperator == null && htblColNameValue.size() > 1) {
			System.err
					.println("Please choose an operator being either \"AND\" or \"OR\" when having multiple columns");
			return null;
		}
		
		
		/*if (htblColNameValue.equals(null) && strOperator == null) {
			
		 this.allTables.get(index).getController().getAllPages();
		 pageIndex.add();
		}*/

		if (strOperator.equals("AND")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageANDSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);

			}

			for (int i = 0; i < pageIndex.size(); i++) {
				if (pageIndex.get(i).getKey()
						.equals(keyGenerator(htblColNameValue))) {
					this.allTables.get(index).getController()
							.getPage(pageIndex.get(i).getPage())
							.read(pageIndex.get(i).getIndex());

				}
			}
			return iter = pageIndex.iterator();
		}

		if (strOperator.equals("OR")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageORSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);
			}

			for (int i = 0; i < pageIndex.size(); i++) {
				this.allTables
						.get(index)
						.getController()
						.getPage(pageIndex.get(i).getPage())
						.read(pageIndex.get(i).getIndex());

			}
			return iter = pageIndex.iterator();
		}

		return null;
	}

	@SuppressWarnings("resource")
	public void CreateIndex (String strTable,String ColName) throws IOException {
	
		String csvFile = "metafile.csv";
		BufferedReader br = null;
		String line = "";
		
		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
			String[] row = line.split(",");
			if (row[0].equals(strTable) && row[1].equals(ColName)){
			System.out.println(row[5]);
			
			System.out.println(row[5]);
			}
		}
		
	}
	
	public static void main(String[] args) throws DBAppException {

		TablesController t = new TablesController();

		Hashtable<String, String> cols = new Hashtable<String, String>();
		cols.put("ID", "int");
		cols.put("name", "date");
		cols.put("DOB", "date");

		Hashtable<String, String> refs = new Hashtable<String, String>();
		refs.put("name", "user.fname");
		refs.put("ID", "employee.ID");
		t.createTable("demo", cols, refs, "name");

		Hashtable<String, String> val = new Hashtable<String, String>();
		val.put("ID", "1");
		val.put("name", "soso");
		val.put("DOB", "1/2/3");

		// t = load();
		t.insertIntoTable("demo", val);

		System.out.println(t.allTables.get(t.searchArraylist("demo"))
				.getController().getCurrentPage());

		// t=load();
		t.insertIntoTable("demo", val);

		int index = t.searchArraylist("demo");

		// System.out.println(t.allTables.get(t.searchArraylist("demo")).getPage().getCurrentPage());

	}

}
