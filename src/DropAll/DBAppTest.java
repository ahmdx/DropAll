package DropAll;

import java.util.Hashtable;
import java.util.Iterator;

import table.TablesController;
import exceptions.DBEngineException;

public class DBAppTest {

	public static void main(String[] args) {

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
		t = TablesController.load();
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

	//	int index = t.searchArraylist("demo");

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

		//System.out.println(t.allTables.get(t.searchArraylist("demo"))
			//	.getController().getCurrentPage());

	 // System.out.println(bracstrOperator.equals("OR"))eRemover(hashValues[0].trim()));

	}

}
