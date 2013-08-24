package jackrabbit.node;

import jackrabbit.util.NodeUtils;

import java.util.Map.Entry;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

public class NodeSizePartitioner implements NodePartitioner {
	
	private long limit;
	
	public NodeSizePartitioner(long limit) {
		this.limit=limit;
	}

	/**
	 * Partition a node based on the size of a node (and its descendants) 
	 * @param node
	 * @param limit
	 * @return a sorted set of Map.Entry with key being the path of a node in the partition and value a Boolean indicating whether the node size
	 * exceeds the limit or not
	 * @throws RepositoryException
	 */
	public Set<Entry<String, Boolean>> partition(Node node) throws RepositoryException {
		Set<Map.Entry<String, Boolean>> descendants = new TreeSet<Map.Entry<String, Boolean>>(new Comparator<Map.Entry<String, Boolean>>() {
            public int compare(Map.Entry<String, Boolean> a, Map.Entry<String, Boolean> b) {
	         	if (a.getValue()==b.getValue()) 
	         		return (a.getKey().toString().toLowerCase()).compareTo(b.getKey().toString().toLowerCase());
	         	else
	         		return -(a.getValue().toString().toLowerCase()).compareTo(b.getValue().toString().toLowerCase());
            }
		});
		long size=NodeUtils.getPropertiesSize(node);
		NodeIterator children = node.getNodes();
		while (children.hasNext()) {
			Node child=children.nextNode();
			long descendantsSize=NodeUtils.getDescendantsSize(child);
			if (descendantsSize < limit) {
				descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(child.getPath(), false));
			} else {
				descendants.addAll(partition(child));
			}
			size+=descendantsSize;
		}
		if (size < limit){
			descendants.clear();
			descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(node.getPath(), false));
		} else {
			descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(node.getPath(), true));
		}
		return descendants;
	}

}
