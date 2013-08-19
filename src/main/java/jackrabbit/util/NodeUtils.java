/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jackrabbit.util;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for working with JCR nodes
 *
 */
public class NodeUtils {
	
	protected static Log log=LogFactory.getLog(NodeUtils.class);

	/**
	 * Utility method to display the contents of a node and its descendants
	 * @param node
	 * @throws RepositoryException 
	 * @throws Exception
	 */
	public static void explore(Node node) throws RepositoryException  {
		log.info(node.getPath());
        // Skip the jcr:system subtree
        if (node.getName().equals("jcr:system")) {
            return;
        }
        // Output the properties
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            if (property.getDefinition().isMultiple()) {
                // A multi-valued property, print all values
                Value[] values = property.getValues();
                for (int i = 0; i < values.length; i++) {
                	log.info(property.getPath() + " = " + values[i].getString());
                }
            } else {
                // A single-valued property
            	log.info(property.getPath() + " = " + property.getString());
            }
        }
        // Output all the child nodes recursively
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            explore(nodes.nextNode());
        }	
	}
		
	
	/**
	 * Get sum of lengths of the values of all properties of a node
	 * @param properties
	 * @return sum of lengths
	 * @throws ValueFormatException
	 * @throws RepositoryException
	 */
	public static long getPropertiesSize(Node node) throws ValueFormatException, RepositoryException {
		long size=0;
		PropertyIterator properties=node.getProperties();
		while (properties.hasNext()) {
            size+=getPropertySize(properties.nextProperty());
        }
		return size;
	}
	
	private static MyProperty getProperties(PropertyIterator properties) throws ValueFormatException, RepositoryException {
		MyProperty prop=new NodeUtils.MyProperty(0, false);
		while (properties.hasNext()) {
            prop.add(getProperty(properties.nextProperty()));
        }
		return prop;
	}
	
	/**
	 * Get size of value(s) as String of a property
	 * @param property
	 * @return length
	 * @throws ValueFormatException
	 * @throws RepositoryException
	 */
	public static long getPropertySize(Property property) throws ValueFormatException, RepositoryException {
		long size=0;
		if (property.getDefinition().isMultiple()) {
            Value[] values = property.getValues();
            for (int i=0; i < values.length; i++) {
            	size+=values[i].getString().getBytes().length;
            }
        } else {
        	size+=property.getValue().getString().getBytes().length;
        }
		return size;		
	}
	
	private static MyProperty getProperty(Property property) throws ValueFormatException, RepositoryException {
		long size=0;
		boolean isPropertyReference=false;
		if (property.getDefinition().isMultiple()) {
            Value[] values = property.getValues();
            for (int i=0; i < values.length; i++) {
            	size+=values[i].getString().getBytes().length;
            }
        } else {
        	size+=property.getValue().getString().getBytes().length;
    		isPropertyReference=property.getType()==PropertyType.PATH||property.getType()==PropertyType.REFERENCE;
        }
		return new NodeUtils.MyProperty(size, isPropertyReference);		
	}
	
	/**
	 * Get sum of all properties sizes of all descendants of a node, including the node itself
	 * @param node
	 * @return sum of properties sizes
	 * @throws RepositoryException
	 */
	public static long getDescendantsSize(Node node) throws RepositoryException {
		long size=getPropertiesSize(node);
		NodeIterator children = node.getNodes();
        while (children.hasNext()) {
        	size+=getPropertiesSize(children.nextNode());
        }	
		return size;
	}
	
	private static MyProperty getDescendantsProperty(Node node) throws RepositoryException {
		MyProperty property=getProperties(node.getProperties());
		NodeIterator children = node.getNodes();
        while (children.hasNext()) {
        	property.add(getDescendantsProperty(children.nextNode()));
        }	
		return property;
	}
	
	/**
	 * Partition a node based on the size of a node (and its descendants) 
	 * @param node
	 * @param limit
	 * @return a sorted set of Map.Entry with key being the path of a node in the partition and value a Boolean indicating whether the node size
	 * exceeds the limit or not
	 * @throws RepositoryException
	 */
	public static Set<Map.Entry<String, Boolean>> partitionBySize(Node node, long limit) throws RepositoryException {
		Set<Map.Entry<String, Boolean>> descendants = new TreeSet<Map.Entry<String, Boolean>>(new Comparator<Map.Entry<String, Boolean>>() {
            public int compare(Map.Entry<String, Boolean> a, Map.Entry<String, Boolean> b) {
	         	if (a.getValue()==b.getValue()) 
	         		return (a.getKey().toString().toLowerCase()).compareTo(b.getKey().toString().toLowerCase());
	         	else
	         		return -(a.getValue().toString().toLowerCase()).compareTo(b.getValue().toString().toLowerCase());
            }
		});
		long size=getPropertiesSize(node);
		NodeIterator children = node.getNodes();
		while (children.hasNext()) {
			Node child=children.nextNode();
			long descendantsSize=getDescendantsSize(child);
			if (descendantsSize < limit) {
				descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(child.getPath(), false));
			} else {
				descendants.addAll(partitionBySize(child, limit));
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
	
	/**
	 * Partition a node based on the size of a node (and its descendants) and whether it (and its descendants) have properties that are
	 * references
	 * @param node
	 * @param limit - node size limit
	 * @return a sorted set containing the paths of the nodes in the partition and whether a node in the partition has property references
	 * @throws RepositoryException
	 */
	public static Set<Map.Entry<String, Boolean>> partition(Node node, long limit) throws RepositoryException {
		Set<Map.Entry<String, Boolean>> descendants = new TreeSet<Map.Entry<String, Boolean>>(new Comparator<Map.Entry<String, Boolean>>() {
            public int compare(Map.Entry<String, Boolean> a, Map.Entry<String, Boolean> b) {
	         	if (a.getValue()==b.getValue()) 
	         		return (a.getKey().toString().toLowerCase()).compareTo(b.getKey().toString().toLowerCase());
	         	else
	         		return -(a.getValue().toString().toLowerCase()).compareTo(b.getValue().toString().toLowerCase());
            }
		});
		MyProperty myProp=new NodeUtils.MyProperty(0, false);
		PropertyIterator properties = node.getProperties();
	    while (properties.hasNext()) {
	       Property property=properties.nextProperty();
	       MyProperty p=getProperty(property);
	       myProp.add(p);
	       if (p.getHasPropertyReference()) {
	       	 	descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(property.getNode().getPath(), true));
	       }    	   
	    }
		NodeIterator children = node.getNodes();
        while (children.hasNext()) {
        	Node child=children.nextNode();
        	MyProperty p=getDescendantsProperty(child);
        	if (p.getSize() < limit && !p.getHasPropertyReference()) {
	    	    descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(child.getPath(), false));
        	} else {
        		descendants.addAll(partition(child, limit));
        	}
        	myProp.add(p);
        }
        if (myProp.getSize() < limit && !myProp.getHasPropertyReference()) {
        	descendants.clear();
        	descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(node.getPath(), false));
        } else {
        	descendants.add(new AbstractMap.SimpleEntry<String, Boolean>(node.getPath(), true));
        }
        return descendants;
	}
		
	/**
	 * Helper class for storing node size and property reference existence
	 *
	 */
	static class MyProperty {
		private long size;
		private boolean hasPropertyReference;
		
		public long getSize() {
			return this.size;
		}
		public boolean getHasPropertyReference() {
			return this.hasPropertyReference;
		}
		public MyProperty(long size, boolean isPropertyReference) {
			this.size=size;
			this.hasPropertyReference=isPropertyReference;
		}
		public void add(MyProperty other) {
			this.size=this.size+other.size;
			this.hasPropertyReference=this.hasPropertyReference||other.hasPropertyReference;
		}
	}
	

}
