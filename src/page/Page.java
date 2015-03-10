package page;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Page implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int pageSize = 20;
	private int index;
	private String[] records;
	private final String pageName;

	public Page(String pageName) {
		this.records = new String[pageSize];
		this.index = 0;
		this.pageName = pageName;
	}

	public final void insertRowIntoPage(String row) {
		this.records[index] = row;
		index++;
	}

	public final void savePage(String pageName) throws FileNotFoundException,
			IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File(this.pageName+".ser")));
		oos.writeObject(this);
		oos.close();
	}

	public static final Page loadPage(String pageName) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File(pageName+".ser")));
		Page page = (Page) ois.readObject();
		ois.close();
		return page;
	}
	
	public String toString() {
		String result = "";
		for (String row : this.records) {
			if (row != null)
				result += row + "\n";
		}
		return result;
	}
}
