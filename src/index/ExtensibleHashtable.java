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
			bucket.initializeBlock();
			this.buckets[i] = bucket;
		}
	}

	private String appendZeros(String string, int size) {
		int bits = (int) (Math.log(size) / Math.log(2));
		int length = bits - string.length();
		String newString = "";
		for (int i = 0; i < length; i++)
			newString += "0";
		return newString + string;
	}

	private Bucket[] initializeBuckets(int bucketSize) {
		Bucket[] buckets = new Bucket[bucketSize];
		for (int i = 0; i < bucketSize; i++) {
			Bucket bucket = new Bucket();
			buckets[i] = bucket;
			bucket.setKey(this.appendZeros(Integer.toBinaryString(i),
					bucketSize));
		}
		return buckets;
	}

	private Bucket[] splitDirectory() {
		this.bitsCompared++;
		Bucket[] temp = this.initializeBuckets(this.buckets.length * 2);
		for (int i = 0; i < this.buckets.length; i++) {
			String bucketKey = this.buckets[i].getKey();
			for (int j = 0; j < temp.length; j++) {
				String tempKey = temp[j].getKey().substring(0,
						bucketKey.length());
				if (bucketKey.equals(tempKey)) {
					temp[j].setBlockName(this.buckets[i].getBlockName());
				}
			}
		}
		this.buckets = temp;
		temp = null;
		return this.buckets;
	}

	private void splitBlock(Bucket bucket) {
		StringBuilder key = new StringBuilder(bucket.getKey());
		if (bucket.getKey().charAt(bucket.getKey().length() - 1) == '1') {
			key.setCharAt(bucket.getKey().length() - 1, '0');
		} else {
			key.setCharAt(bucket.getKey().length() - 1, '1');
		}
		Bucket secondBucket = this.buckets[Integer.parseInt(key.toString(), 2)];

		Block firstBlock = bucket.getBlock();
		Block secondBlock = new Block();
		int bitsCompared = firstBlock.getBitsCompared();
		for (int i = 0; i < firstBlock.getSize(); i++) {
			if (firstBlock.getKey(i).get("value").charAt(bitsCompared) == '1') {
				secondBlock.addKey(firstBlock.getKey(i));
				firstBlock.removeKey(i);
			}
		}
		bitsCompared++;
		firstBlock.setBitsCompared(bitsCompared);
		secondBlock.setBitsCompared(bitsCompared);
		if ((bucket.getKey().charAt(bucket.getKey().length() - 1) == '0')) {
			secondBucket.setBlockName(secondBlock.getBlockName());
		} else {
			bucket.setBlockName(secondBlock.getBlockName());
			secondBucket.setBlockName(firstBlock.getBlockName());
		}
		firstBlock.save();
		secondBlock.save();
	}

	public void insertIndex(Hashtable<String, String> index) {
		String bits;
		int integerBits;
		Block block;

		do {
			bits = Integer.toBinaryString(index.get("value").hashCode())
					.substring(0, this.bitsCompared);
			integerBits = Integer.parseInt(bits, 2);
			block = this.buckets[integerBits].getBlock();
			if (block.isFull()) {
				if (block.getBitsCompared() == this.bitsCompared) {
					splitDirectory();
				}
				splitBlock(this.buckets[integerBits]);
			}
		} while (block.isFull());

		bits = Integer.toBinaryString(index.get("value").hashCode()).substring(
				0, this.bitsCompared);
		integerBits = Integer.parseInt(bits, 2);
		block = this.buckets[integerBits].getBlock();

		block.addKey(index);
	}
	
	public Hashtable<String, String> getIndex(String value) {
		String bits = Integer.toBinaryString(value.hashCode())
				.substring(0, this.bitsCompared);;
		int integerBits = Integer.parseInt(bits, 2);;
		Block block = this.buckets[integerBits].getBlock();
		
		return block.getKey(value);
	}
	
	public void deleteIndex(String value) {
		String bits = Integer.toBinaryString(value.hashCode())
				.substring(0, this.bitsCompared);;
		int integerBits = Integer.parseInt(bits, 2);;
		Block block = this.buckets[integerBits].getBlock();
		
		block.removeKey(value);
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

	public void print() {
		System.out.println(this.getDirectorySize() + " : "
				+ this.getBitsComapred());
		Bucket[] buck = this.getBuckets();
		for (Bucket b : buck)
			System.out.println(b.getKey() + " : " + b.getBlockName() + " : "
					+ b.getBlock().getBitsCompared());
		System.out.println();
	}
}

class Bucket implements Serializable {
	private static final long serialVersionUID = 1L;
	private String key;
	private String blockName;

	public void initializeBlock() {
		Block block = new Block();
		this.setBlockName(block.save());
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

	public Block getBlock() {
		return Block.load(this.blockName);
	}

	public void deleteBlock() {
		Block.load(this.blockName).delete();
		this.blockName = null;
	}

	public String getBlockName() {
		return blockName;
	}

	public void setBlockName(String blockName) {
		this.blockName = blockName;
	}
}

class Block implements Serializable {
	private static final long serialVersionUID = 1L;
	private int blockSize = 20;
	private ArrayList<Hashtable<String, String>> indexes = new ArrayList<Hashtable<String, String>>();
	private String blockName;

	private int bitsCompared;

	public Block() {
		UUID uuid = UUID.randomUUID(); // generates a unique identifier
		this.blockName = uuid.toString();
		this.bitsCompared = 1;
	}

	public void addKey(Hashtable<String, String> key) {
		// if (this.keys.size() < blockSize)
		this.indexes.add(key);
	}

	public Hashtable<String, String> getKey(String value) {
		for (Hashtable<String, String> key : this.indexes) {
			if (key.get("value").equals(value))
				return key;
		}
		return null;
	}

	public Hashtable<String, String> getKey(int index) {
		return this.indexes.get(index);
	}

	public int getSize() {
		return this.indexes.size();
	}

	public void removeKey(String value) {
		for (Hashtable<String, String> key : this.indexes) {
			if (key.get("value").equals(value)) {
				this.indexes.remove(key);
				return;
			}
		}
	}

	public void removeKey(int index) {
		this.indexes.remove(index);
	}

	public boolean isFull() {
		if (this.indexes.size() == blockSize)
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

	public String getBlockName() {
		return blockName;
	}

	public int getBitsCompared() {
		return bitsCompared;
	}

	public void setBitsCompared(int bitsCompared) {
		this.bitsCompared = bitsCompared;
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
		// Directory dir = new Directory();
		//
		// dir.print();
		// dir.splitDirectory();
		// dir.print();
		// dir.splitBlock(dir.getBuckets()[3]);
		// dir.splitDirectory();
		// dir.print();
		// dir.splitBlock(dir.getBuckets()[7]);
		// dir.print();
	}

}
