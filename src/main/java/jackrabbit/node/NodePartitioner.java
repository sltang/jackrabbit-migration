package jackrabbit.node;

import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public interface NodePartitioner {

	public Set<Map.Entry<String, Boolean>> partition(Node node) throws RepositoryException;

}
