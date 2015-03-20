package index;

import java.io.*;
import java.util.*;

class Directory implements Serializable {
	private static final long serialVersionUID = 1L;
	private Bucket[] buckets = new Bucket[2];
	private int bitsCompared = 1;

	public Directory() {
		for (int i = 0; i < this.buckets.length; i++) {
			Bucket bucket = new Bucket();
			bucket.setKey(Integer.toBinaryString(i));
			this.buckets[i] = bucket;
		}
	}

	public void split() {
		Bucket[] temp = new Bucket[this.buckets.length * 2];
		for (int i = 0; i < temp.length; i++) {
			if (i < this.buckets.length) {
				this.buckets[i].setKey(this.appendZeros(
						this.buckets[i].getKey(), temp.length));
				temp[i] = this.buckets[i];
			} else {
				Bucket bucket = new Bucket();
				bucket.setKey(this.appendZeros(Integer.toBinaryString(i),
						temp.length));
				temp[i] = bucket;
			}
		}
		this.buckets = temp;
		temp = null;
		this.bitsCompared++;
	}

	public int getDirectorySize() {
		return this.buckets.length;
	}

	public int getBitsComapred() {
		return this.bitsCompared;
	}

	public Bucket[] getBuckets() {
		return this.buckets;
	}

	private String appendZeros(String string, int size) {
		int bits = (int) (Math.log(size) / Math.log(2));
		int length = bits - string.length();
		String newString = "";
		for (int i = 0; i < length; i++)
			newString += "0";
		return newString + string;
	}
}

class Bucket implements Serializable {
	private static final long serialVersionUID = 1L;
	private String key;
	private String block;

	public void initializeBlock() {
		Block block = new Block();
		this.block = block.save();
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

	public Block getBlock() {
		return Block.load(this.block);
	}

	public void deleteBlock() {
		Block.load(this.block).delete();
		this.block = null;
	}
}

class Block implements Serializable {
	private static final long serialVersionUID = 1L;
	private int blockSize = 20;
	private ArrayList<Hashtable<String, String>> keys;
	private String blockName;

	public Block() {
		UUID uuid = UUID.randomUUID(); // generates a unique identifier
		this.blockName = uuid.toString();
	}

	public void addKey(Hashtable<String, String> key) {
		// if (this.keys.size() < blockSize)
		this.keys.add(key);
	}

	public Hashtable<String, String> getKey(String value) {
		for (Hashtable<String, String> key : this.keys) {
			if (key.get("value").equals(value))
				return key;
		}
		return null;
	}

	public void removeKey(String value) {
		for (Hashtable<String, String> key : this.keys) {
			if (key.get("value").equals(value)) {
				this.keys.remove(key);
				return;
			}
		}
	}

	public boolean isFull() {
		if (this.keys.size() == blockSize)
			return true;
		return false;
	}

	public String save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("pages/" + this.blockName
							+ ".page")));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this.blockName;
	}

	public static Block load(String blockName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("pages/" + blockName + ".page")));
			Block block = (Block) ois.readObject();
			ois.close();
			return block;
		} catch (Exception e) {
			return null;
		}
	}

	public void delete() {
		File f = new File("pages/" + this.blockName + ".page");
		f.delete();
	}
}

public class ExtensibleHashtable implements Serializable {
	private static final long serialVersionUID = 1L;
	private String tableName, columnName;

	public ExtensibleHashtable(String tableName, String columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
	}

	public void saveIndex() {

	}

	public static void main(String[] args) {
		Directory dir = new Directory();
		Bucket[] buck;
		System.out.println(dir.getDirectorySize() + " : "
				+ dir.getBitsComapred());
		buck = dir.getBuckets();
		for (Bucket b : buck)
			System.out.println(b.getKey());
		System.out.println();
		dir.split();
		System.out.println(dir.getDirectorySize() + " : "
				+ dir.getBitsComapred());
		buck = dir.getBuckets();
		for (Bucket b : buck)
			System.out.println(b.getKey());
		System.out.println();
		dir.split();
		System.out.println(dir.getDirectorySize() + " : "
				+ dir.getBitsComapred());
		buck = dir.getBuckets();
		for (Bucket b : buck)
			System.out.println(b.getKey());
		System.out.println();
	}

}
