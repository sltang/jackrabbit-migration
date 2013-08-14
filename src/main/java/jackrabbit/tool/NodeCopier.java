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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodeCopier {
	
	protected static Log log=LogFactory.getLog(NodeCopier.class);
	
	public static void copy(Session srcSession, Session destSession, String srcPath, String destPath, boolean createNodeType) throws RepositoryException, IOException {
		if (!srcSession.nodeExists(srcPath)) {
			log.error(srcPath+ " does not exist");
			return;
		}
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		ByteArrayInputStream in=null;
		try {
			srcSession.exportDocumentView(srcPath, out, true, false);
			in=new ByteArrayInputStream(out.toByteArray());
			createNodes(srcSession, destSession, destPath, createNodeType);
			try {
				destSession.getWorkspace().importXML(destPath, in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
				log.info("Node copied from "+srcPath+" to "+destPath);
			} catch (Exception e) {
				log.error(e.getCause(), e);
			}
		} finally {
			if (in!=null) {
				IOUtils.closeQuietly(in);
			}
			IOUtils.closeQuietly(out);
		}
	}
	
	/**
	 * create nodes along destPath with appropriate node type if they do not already exist
	 * @param destSession
	 * @param path
	 * @param createNodeType whether to create 
	 * @throws RepositoryException
	 */
	private static void createNodes(Session srcSession, Session destSession, String path, boolean createNodeType) throws RepositoryException {
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
				if (createNodeType && srcSession.itemExists(cumuPath))
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

}
