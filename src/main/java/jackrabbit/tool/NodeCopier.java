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

package jackrabbit.tool;

import jackrabbit.util.NodeUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodeCopier {
	
	protected static Log log=LogFactory.getLog(NodeCopier.class);
	
	/**
	 * Copy node with srcPath from one repository to another using the export and import functions
	 * @param srcSession
	 * @param destSession
	 * @param srcPath
	 * @param destPath
	 * @param relPath 
	 * @param createNodeType
	 * @throws RepositoryException
	 * @throws IOException
	 */
	public static void copy(Session srcSession, Session destSession, String srcPath, String destPath, boolean addNodeType) throws RepositoryException, IOException {
		createNodes(srcSession, destSession, destPath, addNodeType);
		copy(srcSession, destSession, srcPath, destPath, "", false, addNodeType);
	}
	
	/**
	 * Copy node with srcPath from one repository to another using the export and import functions
	 * @param srcSession
	 * @param destSession
	 * @param srcPath
	 * @param destPath
	 * @param relPath 
	 * @param noRecurse - Boolean to indicate whether to copy the node recursively
	 * @param createNodeType
	 * @throws RepositoryException
	 * @throws IOException
	 */
	public static void copy(Session srcSession, Session destSession, String srcPath, String destPath, String relPath, boolean noRecurse, boolean addNodeType) throws RepositoryException, IOException {
		if (!srcSession.nodeExists(srcPath)) {
			log.error(srcPath+ " does not exist");
			return;
		}
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		ByteArrayInputStream in=null;
		String nodeName=srcPath.replaceAll(".*/(\\w+)$", "$1");
		try {
			srcSession.exportSystemView(srcPath+relPath, out, true, noRecurse);
			in=new ByteArrayInputStream(out.toByteArray());
			try {
				if (!relPath.isEmpty()) {
					relPath="/"+nodeName+relPath.substring(0, relPath.lastIndexOf("/"));
					createNodes(srcSession, destSession, srcPath, destPath, relPath, addNodeType);
					//to avoid copying the same node twice
					if (destSession.itemExists(destPath+relPath+"/"+nodeName)) {
						return;
					}
				}
				//use session.importXML instead of Workspace.importXML to prevent constraint violations
				destSession.importXML(destPath+relPath, in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
				destSession.save();
				log.info(srcSession.getWorkspace().getName()+" workspace: node copied from "+srcPath+" to "+destPath+relPath+ " with relative path " + relPath);
			 } catch (ItemNotFoundException e) {
		         //do nothing
			 } catch (ConstraintViolationException e) {
		         //do nothing
			 }
		} finally {
			if (in!=null) {
				IOUtils.closeQuietly(in);
			}
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * Copy node with srcPath from one repository to another using the export and import functions by first partitioning node to subnodes of size less than 
	 * limit before exporting
	 * @param srcSession
	 * @param destSession
	 * @param srcPath 
	 * @param destPath 
	 * @param limit - size of a node in the partition
	 * @param createNodeType
	 * @throws RepositoryException
	 * @throws IOException
	 */
	public static void copy(Session srcSession, Session destSession, String srcPath, String destPath, long limit, boolean addNodeType) throws RepositoryException, IOException {
		if (!srcSession.nodeExists(srcPath)) {
			log.error(srcPath+ " does not exist");
			return;
		}
		createNodes(srcSession, destSession, destPath, addNodeType);
		Node node=srcSession.getNode(srcPath);
		int srcPathLength=srcPath.length();
		Set<Map.Entry<String, Boolean>> descendants=NodeUtils.partition(node, limit);
		for (Map.Entry<String, Boolean> entry: descendants) {
			String relPath=entry.getKey().substring(srcPathLength);
			copy(srcSession, destSession, srcPath, destPath, relPath, entry.getValue(), addNodeType);
		}		
	}
	
	/**
	 * create nodes along path in destination workspace with node type from source workspace if they do not exist
	 * @param srcSession
	 * @param destSession
	 * @param path
	 * @param addNodeType whether to create 
	 * @throws RepositoryException
	 */
	private static void createNodes(Session srcSession, Session destSession, String path, boolean addNodeType) throws RepositoryException {
		if (destSession.nodeExists(path)) 
			return;
		Node parent=destSession.getRootNode();
		String cumuPath="";
		String[] nodeNames=path.split("/");
		Node child;
		for (int i=0;i<nodeNames.length;i++) {
			if (nodeNames[i].isEmpty())
				continue;
			cumuPath+="/"+nodeNames[i];
			if (!parent.hasNode(nodeNames[i])) {
				if (addNodeType && srcSession.itemExists(cumuPath))
					child=parent.addNode(nodeNames[i], srcSession.getNode(cumuPath).getPrimaryNodeType().getName());
				else
					child=parent.addNode(nodeNames[i]);
			} else {
				child=parent.getNode(nodeNames[i]);
			}
			parent=child;
		}		
		destSession.save();		
	}
	
	/**
	 * create nodes along relPath in destination workspace with node type from source workspace if they do not exist 
	 * @param srcSession
	 * @param destSession
	 * @param srcPath
	 * @param destPath
	 * @param relPath 
	 * @param addNodeType
	 * @throws RepositoryException
	 */
	private static void createNodes(Session srcSession, Session destSession, String srcPath, String destPath, String relPath, boolean addNodeType) throws RepositoryException {
		//srcPath without last node name
		String cumuPath=srcPath.replaceAll("(.*)/\\w+$", "$1"); 
		//log.info("dest path: "+destPath+relPath);
		if (destSession.nodeExists(destPath+relPath)) 
			return;
		String[] nodeNames=relPath.split("/");
		Node parent=destSession.getNode(destPath);
		for (int i=0;i<nodeNames.length;i++) {
			String nodeName=nodeNames[i];
			if (nodeName.isEmpty())
				continue;
			cumuPath+="/"+nodeName;			
			if (!parent.hasNode(nodeName)) {
				//log.info(srcSession.getWorkspace().getName()+" adding node: "+nodeName+" to "+parent.getPath());
				if (addNodeType && srcSession.itemExists(cumuPath))
					parent.addNode(nodeName, srcSession.getNode(cumuPath).getPrimaryNodeType().getName());
				else
					parent.addNode(nodeName);
			}
			parent=parent.getNode(nodeName);
		}		
		destSession.save();		
	}

}
