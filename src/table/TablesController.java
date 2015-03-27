package table;

import index.ExtensibleHashtable;

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
	private File csvFile = new File("metaFile.csv");

	public TablesController() {
	}

	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
					throws DBAppException {

		int index = searchArraylist(strTableName);
		if (index != -1) {
			throw new DBAppException("Table with name: \"" + strTableName
					+ "\"already exists");
		}
		tableColumns = htblColNameType;
		tableReferences = htblColNameRefs;

		PrintWriter writer;
		FileReader csvReader;
		BufferedReader buffCsvReader;
		boolean empty = false;
		try {
			csvReader = new FileReader(csvFile);
			buffCsvReader = new BufferedReader(csvReader);
			if (buffCsvReader.readLine() == null) {
				empty = true;
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (empty) {
			try {
				writer = new PrintWriter(new FileWriter(csvFile, true));

				for (int i = 0; i < formatList.length; i++) {
					if (i == formatList.length - 1) {
						writer.print(formatList[i]);
					} else {
						writer.print(formatList[i].trim() + "," + " ");
					}
				}

				writer.println();
				writer.println();
				writeIntoCSVFile(strTableName, htblColNameType,
						htblColNameRefs, strKeyColName, writer);

			} catch (Exception e) {

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

		/*
		 * if(keyValue.length != nameType.length){ System.err.println(
		 * "Number of columns to be entered does not match the number of columns of the table"
		 * ); return; }
		 */

		if (index == -1) {
			throw new DBAppException("Please ensure that the table name: \""
					+ strTableName + "\" is correct");
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			if (formatChecker(
					this.allTables.get(index).getColTypes()
					.get(braceRemover(hashValues[0].trim())),
					braceRemover(hashValues[1].trim())) != "true") {

				throw new DBAppException(formatChecker(
						this.allTables.get(index).getColTypes()
						.get(braceRemover(hashValues[0].trim())),
						braceRemover(hashValues[1].trim()))
						+ " in: " + braceRemover(hashValues[0].trim()) + "\n");
			}
		}

		
		try {
			if(pkChecker(strTableName, htblColNameValue, index)){
				throw new DBAppException("The values:\""+htblColNameValue.toString()+"\" you are trying to insert already exist in:\""+strTableName+"\"");
			}
			
			allTables.get(index).getController().writeToPage(htblColNameValue);
		} catch (DBEngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (this.allTables.get(index).getColSingleIndexName().size() > 0) {
			String[] indexTable = this.allTables.get(index)
					.getColSingleIndexName().toString().split(",");
			;
			String[] hashIndex;
			ExtensibleHashtable extTable;
			for (int i = 0; i < indexTable.length; i++) {
				hashIndex = indexTable[i].split("=");
				extTable = ExtensibleHashtable.load(braceRemover(hashIndex[1]
						.trim()));
				extTable.addIndex(htblColNameValue);

			}
		}

		this.save();
	}

	// bulk insert

	public void insertIntoTable(String strTableName,
			ArrayList<Hashtable<String, String>> htblColNameValue)
					throws DBAppException {

		int index = searchArraylist(strTableName);
		String[] keyValue;
		String[] hashValues;
		String[] nameType = this.allTables.get(index).getColTypes().toString()
				.split(",");
		String[] hashTypes;

		if (index == -1) {
			throw new DBAppException("Please ensure that the table name: \""
					+ strTableName + "\" is correct");
		}

		for (int i = 0; i < htblColNameValue.size(); i++) {
			keyValue = htblColNameValue.get(i).toString().split(",");

			for (int j = 0; j < keyValue.length; j++) {
				hashValues = keyValue[i].split("=");
				hashTypes = nameType[i].split("=");
				if (formatChecker(braceRemover(hashTypes[1].trim()),
						braceRemover(hashValues[1].trim())) != "true") {

					throw new DBAppException(formatChecker(
							this.allTables.get(index).getColTypes()
							.get(braceRemover(hashValues[0].trim())),
							braceRemover(hashValues[1].trim()))
							+ " in: "
							+ braceRemover(hashValues[0].trim())
							+ "\n");
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

		if (index == -1) {
			throw new DBEngineException("Please ensure that the table name: \""
					+ strTableName + "\" is correct");
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			if (!this.allTables.get(index).getColTypes()
					.containsKey(braceRemover(hashValues[0].trim()))) {

				throw new DBEngineException("Column \""
						+ braceRemover(hashValues[0].trim())
						+ "\" does not exist in table \"" + strTableName + "\"");
			}

		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			if (formatChecker(
					this.allTables.get(index).getColTypes()
					.get(braceRemover(hashValues[0].trim())),
					braceRemover(hashValues[1].trim())) != "true") {

				throw new DBEngineException(formatChecker(
						this.allTables.get(index).getColTypes()
						.get(braceRemover(hashValues[0].trim())),
						braceRemover(hashValues[1].trim()))
						+ " in: " + braceRemover(hashValues[0].trim()) + "\n");
			}
		}

		int allPagesCount = this.allTables.get(index).getController()
				.getAllPages().length;
		ArrayList<Tuples> pageIndex = new ArrayList<Tuples>(1);

		if (htblColNameValue.equals(null)
				&& strOperator.toLowerCase().trim().equals("null")) {
			this.allTables.get(index).getController().deleteAllPages();
			return;
		}

		if (strOperator.toLowerCase().trim().equals("null")
				&& htblColNameValue.size() > 1) {
			throw new DBEngineException(
					"Please choose an operator being either \"AND\" or \"OR\" when having multiple columns");
		}

		if (strOperator.toLowerCase().trim().equals("and")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageANDSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);
			}

			String key = keyGenerator(htblColNameValue);

			for (int i = 0; i < pageIndex.size(); i++) {
				if (pageIndex.get(i).getKey().contains(key)
						|| pageIndex.get(i).getKey().contains(reverser(key))) {
					this.allTables
					.get(index)
					.getController()
					.deleteFromPage(pageIndex.get(i).getPage(),
							pageIndex.get(i).getIndex());
				}
			}
		}

		if (strOperator.toLowerCase().trim().equals("or")
				|| strOperator.toLowerCase().trim().equals("null")) {
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

	public Iterator<Tuples> selectFromTable(String strTable,
			Hashtable<String, String> htblColNameValue, String strOperator)
					throws DBEngineException {

		int index = searchArraylist(strTable);
		// String[] nameType =
		// this.allTables.get(index).getColTypes().toString().split(",");
		// String[] hashTypes;
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;

		if (index == -1) {
			throw new DBEngineException("Please ensure that the table name: \""
					+ strTable + "\" is correct");
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			if (!this.allTables.get(index).getColTypes()
					.containsKey(braceRemover(hashValues[0].trim()))) {

				throw new DBEngineException("Column \""
						+ braceRemover(hashValues[0].trim())
						+ "\" does not exist in table \"" + strTable + "\"");
			}

		}

		if ((strOperator.trim().toLowerCase().equals("or") || strOperator
				.trim().toLowerCase().equals("and"))
				&& htblColNameValue.size() == 1) {
			throw new DBEngineException(
					"Please add more columns to the query if using an operator");
		}

		for (int i = 0; i < keyValue.length; i++) {
			hashValues = keyValue[i].split("=");
			if (formatChecker(
					this.allTables.get(index).getColTypes()
					.get(braceRemover(hashValues[0].trim())),
					braceRemover(hashValues[1].trim())) != "true") {

				throw new DBEngineException(formatChecker(
						this.allTables.get(index).getColTypes()
						.get(braceRemover(hashValues[0].trim())),
						braceRemover(hashValues[1].trim()))
						+ " in: " + braceRemover(hashValues[0].trim()) + "\n");
			}
		}

		int allPagesCount = this.allTables.get(index).getController()
				.getAllPages().length;
		ArrayList<Tuples> pageIndex = new ArrayList<Tuples>(1);
		Iterator iter;

		if (strOperator.toLowerCase().trim().equals("null")
				&& htblColNameValue.size() > 1) {

			throw new DBEngineException(
					"Please choose an operator being either \"AND\" or \"OR\" when having multiple columns");

		}

		/*
		 * if (htblColNameValue.equals(null) && strOperator == null) {
		 * 
		 * this.allTables.get(index).getController().getAllPages();
		 * pageIndex.add(); }
		 */

		if (strOperator.toLowerCase().trim().equals("or")
				|| strOperator.toLowerCase().trim().equals("null")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageORSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);
			}

			for (int i = 0; i < pageIndex.size(); i++) {
				this.allTables.get(index).getController()
				.getPage(pageIndex.get(i).getPage())
				.read(pageIndex.get(i).getIndex());

			}
			return iter = pageIndex.iterator();
		}

		if (strOperator.toLowerCase().trim().equals("and")) {
			for (int i = 0; i < allPagesCount; i++) {
				pageANDSearcher(this.allTables.get(index).getController()
						.getPage(i), htblColNameValue, i, pageIndex);
			}

			for (int i = 0; i < pageIndex.size(); i++) {
				this.allTables.get(index).getController()
				.getPage(pageIndex.get(i).getPage())
				.read(pageIndex.get(i).getIndex());

			}
			String key = keyGenerator(htblColNameValue);
			for (int i = 0; i < pageIndex.size(); i++) {
				if (!pageIndex.get(index).equals(key)
						|| !pageIndex.get(index).equals(reverser(key))) {
					pageIndex.remove(index);
				}
			}

			return iter = pageIndex.iterator();
		}

		return null;
	}

	public void createIndex(String strTableName, String strColName)
			throws DBAppException {

		BufferedReader br = null;
		String line = "";
		String input = "";

		int index = searchArraylist(strTableName);
		if (index == -1) {
			throw new DBAppException("Table name:\"" + strTableName
					+ " \"does not exist");
		}

		if (!this.allTables.get(index).getColTypes().containsKey(strColName)) {
			throw new DBAppException("Column: \"" + strColName
					+ " \"does not exist in table: \"" + strTableName + "\"");

		}

		if (this.allTables.get(index).getColSingleIndexName()
				.containsKey(strColName)) {
			throw new DBAppException("Column: \"" + strColName
					+ "\" in table: \"" + strTableName
					+ "\" is already indexed");

		}

		try {
			br = new BufferedReader(new FileReader(csvFile));

			while ((line = br.readLine()) != null) {
				String[] column = line.split(",");

				if (column[0].trim().equals(strTableName)
						&& column[2].trim().equals(strColName)) {
					column[4] = " True";

					line = "";
					for (int i = 0; i < column.length; i++) {
						if (i == column.length - 1) {
							line += column[i];
						} else {
							line += column[i] + ",";
						}
					}
				}
				input += line + '\n';
			}
			FileOutputStream fileOut = new FileOutputStream(csvFile);
			fileOut.write(input.getBytes());
			fileOut.close();
			br.close();

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ExtensibleHashtable extHashtable = new ExtensibleHashtable();
		this.allTables.get(index).getColSingleIndexName()
		.put(strColName, extHashtable.getIndexName());
		ArrayList<Tuples> pageIndex;
		ArrayList<Hashtable<String, String>> colVal = new ArrayList<Hashtable<String, String>>(); // values
		// of
		// the
		// column
		int pageNumber;
		int arrayIndex;

		pageIndex = columnProject(strTableName, strColName);

		for (int i = 0; i < pageIndex.size(); i++) {
			if (pageIndex.get(i) == null) {
				this.save();
				return;
			}
			pageNumber = pageIndex.get(i).getPage();
			arrayIndex = pageIndex.get(i).getIndex();
			colVal.add(this.allTables.get(index).getController()
					.getPage(pageNumber).read(arrayIndex));
		}

		for (int i = 0; i < colVal.size(); i++) {
			extHashtable.addIndex(colVal.get(i));
		}

		this.save();

	}

	public ArrayList<Tuples> columnProject(String strTable, String column) {

		int index = searchArraylist(strTable);

		int allPagesCount = this.allTables.get(index).getController()
				.getAllPages().length;
		ArrayList<Tuples> pageIndex = new ArrayList<Tuples>(1);

		for (int i = 0; i < allPagesCount; i++) {
			projectionSearcher(this.allTables.get(index).getController()
					.getPage(i), column, i, pageIndex);
		}

		for (int i = 0; i < pageIndex.size(); i++) {
			this.allTables.get(index).getController()
			.getPage(pageIndex.get(i).getPage())
			.read(pageIndex.get(i).getIndex());

		}

		return pageIndex;

	}

	private void pageANDSearcher(Page page,
			Hashtable<String, String> htblColNameValue, int p,
			ArrayList<Tuples> pageIndex) {
		String[] keyValue = htblColNameValue.toString().split(",");
		String[] hashValues;
		Tuples t;
		String key;
		Hashtable<String, String> tmp;

		for (int j = 0; j < 100; j++) {
			for (int z = 0; z < keyValue.length; z++) {
				hashValues = keyValue[z].split("=");

				tmp = page.read(j);
				if (tmp != null
						&& tmp.get(braceRemover(hashValues[0].trim())).equals(
								braceRemover(hashValues[1].trim()))) {
					int index = arrayListSearcher(pageIndex, p, j);

					if (index != -1) {
						key = pageIndex.get(index).getKey();
						if (key == null) {
							key = "";
						}

						pageIndex.get(index).setKey(
								key.concat(braceRemover(hashValues[0].trim())));

					} else {
						t = new Tuples(p, j, braceRemover(hashValues[0].trim()));
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
		Hashtable<String, String> tmp;

		for (int j = 0; j < 100; j++) {
			for (int z = 0; z < keyValue.length; z++) {
				hashValues = keyValue[z].split("=");
				tmp = page.read(j);
				if (tmp != null
						&& tmp.get(braceRemover(hashValues[0].trim())).equals(
								braceRemover(hashValues[1].trim()))) {
					t = new Tuples(p, j, braceRemover(hashValues[0].trim()));

					if (!contains(pageIndex, p, j)) {
						pageIndex.add(t);
					}
				}
			}
		}
	}

	private void projectionSearcher(Page page, String column, int p,
			ArrayList<Tuples> pageIndex) {
		Tuples t;
		Hashtable<String, String> tmp;

		for (int j = 0; j < 100; j++) {

			tmp = page.read(j);
			if (tmp != null && tmp.containsKey(column)) {
				t = new Tuples(p, j, column);

				if (!contains(pageIndex, p, j)) {
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
			concat = concat.concat(braceRemover(hash[0].trim()));
		}
		return concat;
	}

	private boolean pkChecker(String strTableName, Hashtable<String, String> htblColNameValue, int index) throws DBEngineException {
		String operator = "";
		if(htblColNameValue.size() == 1){
			 operator = "null";
		} else { 
			if(htblColNameValue.size() > 1){
				operator = "and";
			} 
		}

		Iterator<Tuples> i = selectFromTable(strTableName, htblColNameValue, operator);
		
		if(!i.hasNext()){
			return false;
		} else {

			return true;
		}

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

	private String braceRemover(String x) { // adjusts the name key in a
		// hashtable
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

		String pattern = "^(\\w+)(\\.)(\\w+)$";
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
			pattern = "^(\\d\\d|\\d)(/)(\\d\\d|\\d)(/)(\\d\\d\\d\\d)$";
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

	private void writeIntoCSVFile(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName,
			PrintWriter writer) throws DBAppException {

		String[] hash;
		String error;
		String[] tableColumns = htblColNameType.toString().split(",");
		Hashtable<String, String> tmpColType = new Hashtable<String, String>();
		String tmpString;

		if (htblColNameRefs != null) {
			error = referencesTableFormat(htblColNameRefs);

			if (error != null) {
				throw new DBAppException(
						"Error in referencing another table\nPlease specify the column name you want to refer to in: "
								+ braceRemover(error)
								+ "\nTo specifiy column place a \".\" after the table name");
			}
		}

		for (int i = 0; i < tableColumns.length; i++) {
			hash = tableColumns[i].split("=");
			writer.print(strTableName + "," + " "); // writing table name
			writer.print(keyHelper(strKeyColName, braceRemover(hash[0]))); // writing
			// if
			// column
			// is
			// PK
			writer.print(braceRemover(hash[0].trim()) + "," + " "); // writing
			// column
			// name

			if (typeHelper(hash[1].trim()) == hash[1].trim()) {
				throw new DBAppException("unkown data type in: "
						+ hash[0].trim() + " ==> " + hash[1].trim());

			}

			writer.print(typeHelper(hash[1].trim())); // writing column type
			tmpString = typeHelper(hash[1].trim());
			tmpColType.put(braceRemover(hash[0].trim()),
					tmpString.substring(0, tmpString.length() - 2));
			writer.print("False, "); // writing index
			if (htblColNameRefs != null) {
				writer.print(fkHelper(htblColNameRefs, braceRemover(hash[0]))); // writing
			} else {
				writer.print("null");
			}
			// references
			writer.println();
		}
		writer.println();
		writer.close();
		ArrayList<String> tmp = new ArrayList<String>();
		tmp.add(strKeyColName);

		tableObject = new Table(strTableName, tmpColType, htblColNameRefs);
		tableObject.setColPK(tmp);
		allTables.add(tableObject);
		this.save();

	}

	public static String reverser(String x) {
		if (x.length() <= 1)
			return x;
		else {
			return reverser(x.substring(1)) + x.charAt(0);
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

	public static void main(String[] args) throws DBAppException, IOException {

		TablesController t = new TablesController();
		// t.allTables.clear();
		Hashtable<String, String> cols = new Hashtable<String, String>();
		cols.put("ID", "int");
		cols.put("name", "varchar");
		cols.put("DOB", "date");

		Hashtable<String, String> refs = new Hashtable<String, String>();
		refs.put("name", "user.fname");
		refs.put("ID", "employee.ID");
		// t=load();
		// t.createTable("demo", cols, null, "name");
		// t.createTable("demo", cols, null, "name");

		Hashtable<String, String> val = new Hashtable<String, String>();

		/*
		 * val.put("ID", "1"); val.put("name", "soso"); val.put("DOB",
		 * "13/22/3333");
		 */
		t = load();
		// t.insertIntoTable("demo", val);

		/*
		 * val.put("ID", "2"); val.put("name", "sasso"); val.put("DOB",
		 * "13/22/3333"); t.insertIntoTable("demo", val);
		 */
		/*
		 * val.put("ID", "3"); val.put("name", "soso"); val.put("DOB",
		 * "13/22/3333");
		 * 
		 * t.insertIntoTable("demo", val);
		 */
		//	t.createIndex("demo", "name");
		// System.out.println(t.allTables.get(t.searchArraylist("demo")).getControlwent
		// here"ler().getCurrentPage());

		// t=load();
		// t.insertIntoTable("demo", val);

		int index = t.searchArraylist("demo");

		Hashtable<String, String> x = new Hashtable<String, String>();
		x.put("DOB", "1/22/3333");
		// x.put("name", "sasso");


		try { Iterator i= t.selectFromTable("demo", x, "null");
		System.out.println(i.hasNext()); 

		for(int z =0 ; i.hasNext() != false; z++){ i.next();
		System.out.println(z); } if(i == null) System.out.println(232);

		} catch (DBEngineException e) { 
			e.printStackTrace();

		}

		/*
		 * try { t.deleteFromTable("demo", x, "and"); } catch (DBEngineException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		// System.out.println(t.allTables.get(index).getColTypes().toString());

		System.out.println(t.allTables.get(t.searchArraylist("demo"))
				.getController().getCurrentPage());

	} // System.out.println(bracstrOperator.equals("OR"))eRemover(hashValues[0].trim()));

}
