package index;

import java.io.*;
import java.util.*;

class KDNode implements Serializable {
	private static final long serialVersionUID = 1L;
	private String key;
	private KDNode leftNode;
	private KDNode rightNode;
	private String leftBlock;
	private String rightBlock;
	private int level;

	public KDNode(String key) {
		System.out.println("KEY: " + key);
		this.setKey(key);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public KDNode getLeftNode() {
		return leftNode;
	}

	public void setLeftNode(KDNode leftNode) {
		this.leftNode = leftNode;
	}

	public KDNode getRightNode() {
		return rightNode;
	}

	public void setRightNode(KDNode rightNode) {
		this.rightNode = rightNode;
	}

	public String getLeftBlock() {
		return leftBlock;
	}

	public void setLeftBlock(String leftBlock) {
		this.leftBlock = leftBlock;
	}

	public String getRightBlock() {
		return rightBlock;
	}

	public void setRightBlock(String rightBlock) {
		this.rightBlock = rightBlock;
	}

	public KDBlock getBlock(String blockName) {
		return KDBlock.load(blockName);
	}

	public boolean isLeaf() {
		return (this.leftNode == null && this.rightNode == null);
	}

	public boolean isFull() {
		return (this.getBlock(this.leftBlock).isFull() && this.getBlock(
				this.rightBlock).isFull());
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}

class KDBlock implements Serializable {
	private static final long serialVersionUID = 1L;
	private int blockSize = 25;
	private ArrayList<Hashtable<String, String>> indexes = new ArrayList<Hashtable<String, String>>();
	private String blockName;

	public KDBlock() {
		UUID uuid = UUID.randomUUID(); // generates a unique identifier
		this.blockName = uuid.toString();
		System.out.println("BLOCK____________________________ "
				+ this.blockName);
	}

	public void addKey(Hashtable<String, String> key) {
		// if (this.keys.size() < blockSize)
		this.indexes.add(key);
	}

	public Hashtable<String, String> getKey(Hashtable<String, String> value) {
		// Set<String> keys = value.keySet();
		// boolean found = false;
		for (Hashtable<String, String> index : this.indexes) {
			if (this.valueFound(value, index)) {
				return index;
			}
		}
		return null;
	}

	public boolean valueFound(Hashtable<String, String> value,
			Hashtable<String, String> index) {
		Set<String> keys = value.keySet();
		for (String key : keys) {
			if (!value.get(key).equals(index.get(key))) {
				return false;
			}
		}
		return true;
	}

	public void removeKey(Hashtable<String, String> value) {
		for (Hashtable<String, String> index : this.indexes) {
			if (this.valueFound(value, index)) {
				this.indexes.remove(index);
				return;
			}
		}
	}

	public Hashtable<String, String> getKey(int index) {
		return this.indexes.get(index);
	}

	public int getSize() {
		return this.indexes.size();
	}

	public void removeKey(int index) {
		this.indexes.remove(index);
	}

	public boolean isFull() {
		if (this.indexes.size() == this.blockSize)
			return true;
		return false;
	}

	public String getBlockName() {
		return blockName;
	}

	public ArrayList<Hashtable<String, String>> getIndexes() {
		return this.indexes;
	}

	public String save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("data/pages/"
							+ this.blockName + ".page")));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return this.blockName;
	}

	public static KDBlock load(String blockName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("data/pages/" + blockName + ".page")));
			KDBlock block = (KDBlock) ois.readObject();
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

	public void print() {
		for (Hashtable<String, String> index : this.indexes) {
			System.out.println("          " + index);
		}
	}
}

public class KDTree implements Serializable {
	private static final long serialVersionUID = 1L;
	private String indexName;
	private ArrayList<String> keys;
	private int k;
	private KDNode root;

	public KDTree(ArrayList<String> keys) {
		this.indexName = UUID.randomUUID().toString();
		this.keys = keys;
		this.k = keys.size();
		this.save();
	}

	private KDNode initNode(String key, int level) {
		System.out.println("INIT___________--------------_________________");
		KDNode node = new KDNode(key);
		node.setLevel(level);
		KDBlock leftBlock = new KDBlock();
		KDBlock rightBlock = new KDBlock();
		node.setLeftBlock(leftBlock.getBlockName());
		node.setRightBlock(rightBlock.getBlockName());
		leftBlock.save();
		rightBlock.save();
		return node;
	}

	private String getMedian(KDBlock block, String key, String value) {
		String[] strings = new String[block.getSize() + 1];
		ArrayList<Hashtable<String, String>> indexes = block.getIndexes();
		int i = 0;
		for (Hashtable<String, String> index : indexes) {
			strings[i] = index.get(key);
			i++;
		}
		strings[block.getSize()] = value;
		Arrays.sort(strings);
		System.out.println(key);
		System.out.println(Arrays.deepToString(strings));
		i = strings.length;
		return (i % 2 == 0) ? strings[(i / 2) + 1] : strings[i / 2];
	}

	private void ditributeKeys(KDBlock full, KDBlock empty, String key,
			String nodeKey) {
		ArrayList<Hashtable<String, String>> indexes = full.getIndexes();
		ArrayList<Hashtable<String, String>> temp = new ArrayList<Hashtable<String, String>>();
		for (Hashtable<String, String> index : indexes) {
			if (index.get(key).compareTo(nodeKey) < 0) {
				empty.addKey(index);
				temp.add(index);
			}
		}
		for (Hashtable<String, String> hash : temp) {
			full.removeKey(hash);
		}
		full.save();
		empty.save();
	}

	private void splitBlock(KDNode node, int side,
			Hashtable<String, String> index) {
		KDBlock block, newBlock, target;
		int nextLevel;
		KDNode newNode;
		do {
			newBlock = new KDBlock();
			nextLevel = node.getLevel() + 1;
			if (side == -1) {
				// Left node
				block = node.getBlock(node.getLeftBlock());
				newNode = new KDNode(this.getMedian(block,
						this.keys.get(nextLevel % k),
						index.get(this.keys.get(nextLevel % k))));
				newNode.setLevel(nextLevel);
				node.setLeftNode(newNode);
				node.setLeftBlock(null);
			} else {
				block = node.getBlock(node.getRightBlock());
				newNode = new KDNode(this.getMedian(block,
						this.keys.get(nextLevel % k),
						index.get(this.keys.get(nextLevel % k))));
				newNode.setLevel(nextLevel);
				node.setRightNode(newNode);
				node.setRightBlock(null);
			}
			newNode.setLeftBlock(newBlock.getBlockName());
			newNode.setRightBlock(block.getBlockName());
			node = newNode;
			this.save();
			this.ditributeKeys(block, newBlock, this.keys.get(nextLevel % k),
					newNode.getKey());
			target = KDBlock.load(this.getTargetBlock(index));
			if (!target.isFull()) {
				System.out.println("INSERT");
				target.addKey(index);
				target.save();
			}
		} while (target.isFull());
	}

	private void insertIntoBlock(KDNode node, int side,
			Hashtable<String, String> index) {
		KDBlock block = (side == -1) ? node.getBlock(node.getLeftBlock())
				: node.getBlock(node.getRightBlock());
		if (block.isFull()) {
			this.splitBlock(node, side, index);
		} else {
			System.out.println("INSERT");
			block.addKey(index);
			block.save();
		}
	}

	public void insertIndex(Hashtable<String, String> index) {
		if (this.root == null) {
			KDBlock block;
			this.root = this.initNode(index.get(this.keys.get(0 % this.k)), 0);
			block = this.root.getBlock(this.root.getRightBlock());
			block.addKey(index);
			block.save();
			this.save();
			return;
		}
		this.insertIndex(index, this.root, 0);
		this.save();
	}

	private void insertIndex(Hashtable<String, String> index, KDNode node,
			int level) {
		if (node.isLeaf()) {
			if (index.get(this.keys.get(level % this.k)).compareTo(
					node.getKey()) < 0) {
				// index value < node key
				this.insertIntoBlock(node, -1, index);
			} else {
				this.insertIntoBlock(node, 1, index);
			}
			return;
		} else {
			if (index.get(this.keys.get(level % this.k)).compareTo(
					node.getKey()) < 0) {
				if (node.getLeftNode() != null) {
					this.insertIndex(index, node.getLeftNode(), ++level);
				} else {
					this.insertIntoBlock(node, -1, index);
				}
			} else {
				if (node.getRightNode() != null) {
					this.insertIndex(index, node.getRightNode(), ++level);
				} else {
					this.insertIntoBlock(node, 1, index);
				}
			}
		}
	}

	public Hashtable<String, String> getIndex(Hashtable<String, String> index) {
		KDBlock block = KDBlock.load(this.getTargetBlock(index));
		return block.getKey(index);
	}

	public void deleteIndex(Hashtable<String, String> index) {
		KDBlock block = KDBlock.load(this.getTargetBlock(index));
		block.removeKey(index);
		block.save();
	}

	private String getTargetBlock(Hashtable<String, String> index) {
		return this.getTargetBlock(index, this.root, 0);
	}

	private String getTargetBlock(Hashtable<String, String> index, KDNode node,
			int level) {
		if (node.isLeaf()) {
			// KDBlock block;
			if (index.get(this.keys.get(level % this.k)).compareTo(
					node.getKey()) < 0) {
				// block = node.getBlock(node.getLeftBlock());
				return node.getLeftBlock();
			} else {
				// block = node.getBlock(node.getRightBlock());
				return node.getRightBlock();
			}
		} else {
			if (index.get(this.keys.get(level % this.k)).compareTo(
					node.getKey()) < 0) {
				if (node.getLeftNode() != null) {
					return this.getTargetBlock(index, node.getLeftNode(),
							++level);
				} else {
					return node.getLeftBlock();
				}
			} else {
				if (node.getRightNode() != null) {
					return this.getTargetBlock(index, node.getRightNode(),
							++level);
				} else {
					return node.getRightBlock();
				}
			}
		}
	}

	public String getIndexName() {
		return this.indexName;
	}

	public KDNode getRoot() {
		return root;
	}

	public void setRoot(KDNode root) {
		this.root = root;
	}

	public ArrayList<String> getKeys() {
		return this.keys;
	}

	public int getK() {
		return this.k;
	}

	public void save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("data/indexes/"
							+ this.indexName + ".index")));
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static KDTree load(String indexName) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("data/indexes/" + indexName + ".index")));
			KDTree index = (KDTree) ois.readObject();
			ois.close();
			return index;
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean delete(String indexName) {
		KDTree tree = KDTree.load(indexName);
		tree.deleteBlocks(tree.getRoot());
		File f = new File("data/indexes/" + indexName + ".index");
		return f.delete();
	}

	public void deleteBlocks(KDNode node) {
		if (node == null)
			return;

		if (node.getLeftNode() != null)
			this.deleteLeftBlock(node.getLeftNode());
		else
			node.getBlock(node.getLeftBlock()).delete();

		if (node.getRightNode() != null)
			this.deleteRightBlock(node.getRightNode());
		else
			node.getBlock(node.getRightBlock()).delete();

		// if (node.isLeaf()) {
		// node.getBlock(node.getLeftBlock()).delete();
		// node.getBlock(node.getRightBlock()).delete();
		// return;
		// }
		// this.deleteLeftBlock(node.getLeftNode());
		// this.deleteLeftBlock(node.getRightNode());
	}

	public void deleteLeftBlock(KDNode node) {
		// if (node == null)
		// return;
		// if (node.isLeaf()) {
		// node.getBlock(node.getLeftBlock()).delete();
		// node.getBlock(node.getRightBlock()).delete();
		// return;
		// }
		// if (node.getLeftNode() != null)
		// this.deleteLeftBlock(node.getLeftNode());
		// else
		// node.getBlock(node.getLeftBlock()).delete();
		if (node.getLeftNode() != null)
			this.deleteLeftBlock(node.getLeftNode());
		else
			node.getBlock(node.getLeftBlock()).delete();

		if (node.getRightNode() != null)
			this.deleteRightBlock(node.getRightNode());
		else
			node.getBlock(node.getRightBlock()).delete();
	}

	public void deleteRightBlock(KDNode node) {
		// if (node == null)
		// return;
		// if (node.isLeaf()) {
		// node.getBlock(node.getLeftBlock()).delete();
		// node.getBlock(node.getRightBlock()).delete();
		// return;
		// }
		// if (node.getRightNode() != null)
		// this.deleteRightBlock(node.getRightNode());
		// else
		// node.getBlock(node.getRightBlock()).delete();
		if (node.getLeftNode() != null)
			this.deleteLeftBlock(node.getLeftNode());
		else
			node.getBlock(node.getLeftBlock()).delete();

		if (node.getRightNode() != null)
			this.deleteRightBlock(node.getRightNode());
		else
			node.getBlock(node.getRightBlock()).delete();
	}

	public void print() {
		if (this.root == null) {
			System.out.println("Empty Tree");
		} else {
			print(this.root, 0);
		}
	}

	public void print(KDNode node, int level) {
		System.out.println("Level: " + level + " "
				+ this.keys.get(level % this.k) + ": " + node.getKey());
		if (node.getLeftNode() != null) {
			this.print(node.getLeftNode(), ++level);
		} else {
			System.out.println("    Left Block: " + node.getLeftBlock());
			node.getBlock(node.getLeftBlock()).print();
		}

		if (node.getRightNode() != null) {
			this.print(node.getRightNode(), ++level);
		} else {
			System.out.println("    Right Block: " + node.getRightBlock());
			node.getBlock(node.getRightBlock()).print();
		}

	}

	public static void main(String[] args) {
		ArrayList<String> keys = new ArrayList<String>();
		keys.add("name");
		keys.add("age");
		KDTree tree = new KDTree(keys);
		Hashtable<String, String> index;
		int x = 20;
		int i = x;
		while (i-- > 0) {
			index = new Hashtable<String, String>();
			index.put("name", "name #" + i);
			index.put("age", "" + i);
			tree.insertIndex(index);
		}
//		i = x;
//		while (i-- > 0) {
//			index = new Hashtable<String, String>();
//			index.put("name", "name #" + i);
//			index.put("age", "" + i);
//			tree.deleteIndex(index);
//		}
		i = x;
		while (i-- > 0) {
			index = new Hashtable<String, String>();
			index.put("name", "name #" + i);
			index.put("age", "" + i);
			System.out.println("Found: " + tree.getIndex(index));
		}

	
		
		System.out.println(KDTree.delete(tree.getIndexName()));

		System.out.println();
//		tree.print();
		KDTree.delete(tree.getIndexName());
//		String[] strings = {"1", "2", "3", "4", "5", "6", "7",};
//		Arrays.sort(strings);
//		System.out.println(strings.toString());
		
//		System.out.println("".compareTo("3"));

	}
}
