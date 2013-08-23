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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
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
	
	
}
