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

	public void splitDirectory() {
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
		// return this.buckets;
	}

	public void splitBlock(Bucket bucket) {
		// StringBuilder key = new StringBuilder(bucket.getKey());
		// if (bucket.getKey().charAt(bucket.getKey().length() - 1) == '1') {
		// key.setCharAt(bucket.getKey().length() - 1, '0');
		// } else {
		// key.setCharAt(bucket.getKey().length() - 1, '1');
		// }
		Bucket secondBucket = this.buckets[Integer.parseInt(bucket.getKey()) ^ 1];

		Block firstBlock = bucket.getBlock();
		Block secondBlock = new Block();
		int bitsCompared = firstBlock.getBitsCompared();
		for (int i = 0; i < firstBlock.getSize(); i++) {
			if (Integer.toBinaryString(
					firstBlock.getKey(i).get("value").hashCode()).charAt(
					bitsCompared) == '1') {
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
		block.save();
	}

	public Hashtable<String, String> getIndex(String value) {
		String bits = Integer.toBinaryString(value.hashCode()).substring(0,
				this.bitsCompared);
		;
		int integerBits = Integer.parseInt(bits, 2);
		;
		Block block = this.buckets[integerBits].getBlock();

		return block.getKey(value);
	}

	public void deleteIndex(String value) {
		String bits = Integer.toBinaryString(value.hashCode()).substring(0,
				this.bitsCompared);
		int integerBits = Integer.parseInt(bits, 2);
		Block block = this.buckets[integerBits].getBlock();

		block.removeKey(value);

		if (block.getSize() == 0)
			shrinkBlock(this.buckets[integerBits]);
		block.save();
	}

	public void shrinkBlock(Bucket bucket) {
		bucket.getBlock().delete();
		// StringBuilder key = new StringBuilder(bucket.getKey());
		// if (bucket.getKey().charAt(bucket.getKey().length() - 1) == '1') {
		// key.setCharAt(bucket.getKey().length() - 1, '0');
		// } else {
		// key.setCharAt(bucket.getKey().length() - 1, '1');
		// }
		Bucket secondBucket = this.buckets[Integer.parseInt(bucket.getKey()) ^ 1];

		bucket.setBlockName(secondBucket.getBlockName());
		Block block = bucket.getBlock();
		block.setBitsCompared(block.getBitsCompared() - 1);
		block.save();

		// this.shrinkDirectory();
	}

	private boolean lessBitsCompared() {
		for (int i = 0; i < this.buckets.length; i++) {
			if (this.buckets[i].getBlock().getBitsCompared() == this.bitsCompared)
				return false;
		}
		return true;
	}

	public void shrinkDirectory() {
		while (this.lessBitsCompared() && this.buckets.length > 2) {
			this.bitsCompared--;
			Bucket[] temp = this.initializeBuckets(this.buckets.length / 2);
			for (int i = 0; i < temp.length; i++) {
				int key = Integer.parseInt(temp[i].getKey() + "0", 2);
				temp[i].setBlockName(this.buckets[key].getBlockName());
			}

			this.buckets = temp;
			temp = null;
		}
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
		for (Bucket b : buck) {
			Block block = b.getBlock();
			System.out.println(b.getKey() + " : " + b.getBlockName() + " : "
					+ block.getBitsCompared() + " : " + block.getSize());
			ArrayList<Hashtable<String, String>> indexes = block.getIndexes();
			for (Hashtable<String, String> index : indexes) {
				System.out.println("        " + index);
			}
		}
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
	private int blockSize = 100;
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
					new FileOutputStream(new File("data/pages/" + this.blockName
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
					new File("data/pages/" + blockName + ".page")));
			Block block = (Block) ois.readObject();
			ois.close();
			return block;
		} catch (Exception e) {
			return null;
		}
	}

	public void delete() {
		File f = new File("data/pages/" + this.blockName + ".page");
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

	public ArrayList<Hashtable<String, String>> getIndexes() {
		return this.indexes;
	}
}

public class ExtensibleHashtable implements Serializable {
	private static final long serialVersionUID = 1L;
	private String indexName;
	private Directory directory;

	public ExtensibleHashtable(/* String tableName, String columnName */) {
		// this.tableName = tableName;
		// this.columnName = columnName;
		this.directory = new Directory();
		this.indexName = UUID.randomUUID().toString();
		this.save();
	}

	public void addIndex(Hashtable<String, String> index) {
		this.directory.insertIndex(index);
		this.save();
	}
	
	public void addIndexes(Hashtable<String, String>[] indexes) {
//		for (Hashtable<String, String> index : indexes) {
//		}
//		this.save();
	}

	public Hashtable<String, String> getIndex(String value) {
		return this.directory.getIndex(value);
	}

	
	public void deleteIndex(String value) {
		this.directory.deleteIndex(value);
		this.save();
	}

	public void save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("data/indexes/" + this.indexName
							+ ".index")));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ExtensibleHashtable load(String indexName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("data/indexes/" + indexName + ".index")));
			ExtensibleHashtable index = (ExtensibleHashtable) ois.readObject();
			ois.close();
			return index;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean delete(String indexName) {
		ExtensibleHashtable index = ExtensibleHashtable.load(indexName);
		Bucket[] dir = index.directory.getBuckets();
		for (Bucket bucket : dir) {
			if (bucket.getBlock() != null)
				bucket.getBlock().delete();
		}
		File f = new File("data/indexes/" + indexName + ".index");
		return f.delete();
	}

	public String getIndexName() {
		return indexName;
	}

	public Directory getDirectory() {
		return this.directory;
	}

	public static void main(String[] args) {
		ExtensibleHashtable index = new ExtensibleHashtable();
		// ExtensibleHashtable index = ExtensibleHashtable
		// .load("20dbb6e7-6136-4113-8101-3f71667aa604");
		index.getDirectory().splitDirectory();
		index.getDirectory().splitBlock(index.getDirectory().getBuckets()[1]);
		// index.getDirectory().shrinkBlock(index.getDirectory().getBuckets()[1]);
		// ExtensibleHashtable.delete("20dbb6e7-6136-4113-8101-3f71667aa604");
		// int i = 40;
		// while (i-- > 0) {
		// Hashtable<String, String> key = new Hashtable<String, String>();
		// key.put("value", "" + i);
		// index.addIndex(key);
		// }
		// index.directory.print();
		index.save();
		index.directory.print();

		// System.out.println(Integer.toBinaryString(5 ^ 1));
		ExtensibleHashtable.delete(index.getIndexName());
	}

}
