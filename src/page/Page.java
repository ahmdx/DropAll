package page;

import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;

public class Page implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int pageSize = 100;
	private int index;
	private Hashtable<String, String>[] records;
	private final String pageName;

	public Page(String pageName) {
		this.records = new Hashtable[pageSize];
		this.index = 0;
		this.pageName = pageName;
	}

	public final Hashtable<String, String> write(
			Hashtable<String, String> record) {
		this.records[this.index] = record;
		this.index++;
		Hashtable<String, String> hash = new Hashtable<String, String>();
		hash.put("page", this.pageName);
		hash.put("index", "" + index);
		return hash;
	}

	public final Hashtable<String, String> read(int index) {
		if (index < 0 || index > pageSize)
			return null;
		return this.records[index];
	}

	public final boolean remove(int index) {
		if (index < 0 || index > pageSize)
			return false;
		this.records[index] = null;
		return true;
	}

	public final boolean save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("data/pages/" + this.pageName
							+ ".page")));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static final Page load(String pageName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("data/pages/" + pageName + ".page")));
			Page page = (Page) ois.readObject();
			ois.close();
			return page;
		} catch (Exception e) {
			return null;
		}
	}

	public static final boolean delete(String pageName) {
		File f = new File("data/pages/" + pageName + ".page");
		return f.delete();
	}

	public final Hashtable<String, String>[] readContent() {
		return this.records;
	}

	public final boolean isFull() {
		return this.index == this.records.length;
	}

	public String toString() {
		String result = "";
		for (Hashtable<String, String> row : this.records) {
			if (row != null)
				result += row + "\n";
		}
		return result;
	}

	public static void main(String[] args) {
	}
}
