package page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

public class PageController implements Serializable {
	private ArrayList<String> pages = new ArrayList<String>();
	private String currentPage = "";

	private final boolean createPage() {
		UUID uuid = UUID.randomUUID(); // generates a unique identifier
		String randomUUIDString = uuid.toString();
		Page newPage = new Page(randomUUIDString);
		if (newPage.save()) {
			pages.add(randomUUIDString);
			this.currentPage = randomUUIDString;
			return true;
		}
		return false;
	}

	public final Hashtable<String, String> writeToPage(
			Hashtable<String, String> record) {
		Page page = Page.load(this.currentPage);
		if (page == null || page.isFull()) {
			this.createPage();
			page = Page.load(this.currentPage);
		}
		Hashtable<String, String> hash = page.write(record);
		page.save();
		return hash;
	}

	public final void writeToPage(ArrayList<Hashtable<String, String>> records) {
		Page page = Page.load(this.currentPage);
		if (page == null || page.isFull()) {
			this.createPage();
			page = Page.load(this.currentPage);
		}
		for (Hashtable<String, String> record : records) {
			if (page.isFull()) {
				page.save();
				this.createPage();
				page = Page.load(this.currentPage);
			}
			if (record != null)
				page.write(record);
		}
		page.save();
	}

	public final boolean deleteFromPage(int pageNumber, int recordIndex) {
		if (pageNumber < 0 || pageNumber >= this.pages.size())
			return false;
		Page page = Page.load(this.pages.get(pageNumber));
		page.remove(recordIndex);
		return page.save();
	}

	public final boolean deleteFromPage(int pageNumber, int[] recordIndices) {
		if (pageNumber < 0 || pageNumber >= this.pages.size())
			return false;
		Page page = Page.load(this.pages.get(pageNumber));
		for (int recordIndex : recordIndices)
			page.remove(recordIndex);
		return page.save();
	}

	public final Page getCurrentPage() {
		return Page.load(this.currentPage);
	}

	public final Page getPage(int pageNumber) {
		if (pageNumber < 0 || pageNumber >= this.pages.size())
			return null;
		return Page.load(this.pages.get(pageNumber));
	}

	public final Page[] getAllPages() {
		Page[] pages = new Page[this.pages.size()];
		for (int i = 0; i < pages.length; i++)
			pages[i] = Page.load(this.pages.get(i));
		return pages;
	}

	public final void deleteAllPages() {
		for (String page : this.pages)
			Page.delete(page);
		this.pages.clear();
		this.currentPage = "";
	}

	public static void main(String[] args) {
		// PageController pages = new PageController();
		// int i = 100;
		// Hashtable<String, String>[] r = new Hashtable[101];
		// while(i-- > 0) {
		// r[i] = new Hashtable<String, String>();
		// r[i].put("key #" + i, "" + i);
		// }
		// pages.writeToPage(r);
		//
		// Page[] pag = pages.getAllPages();
		// for (Page p : pag){
		// System.out.println(p);
		// }

		// pages.deleteAllPages();
	}
}
