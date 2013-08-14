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

package jackrabbit.repository;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;

public class RepositoryManager {
	
	private static String SECURITY_WORKSPACE="security";
	private static String DEFAULT_WORKSPACE="default";
	
	protected static Log log=LogFactory.getLog(RepositoryManager.class);
	
	public static List<String> getDestinationWorkspaces(Session src, Session dest) throws IOException, RepositoryException {
		List<String> wsNames=new ArrayList<String>();
		List<String> srcWorkpaces=Arrays.asList(src.getWorkspace().getAccessibleWorkspaceNames());
		List<String> destWorkpaces=Arrays.asList(dest.getWorkspace().getAccessibleWorkspaceNames());
		
		for (String workspace:srcWorkpaces) {
			if (!destWorkpaces.contains(workspace)) {
				dest.getWorkspace().createWorkspace(workspace);
			}
			wsNames.add(workspace);	
		}
		wsNames.remove(DEFAULT_WORKSPACE);
		wsNames.remove(SECURITY_WORKSPACE);
		return wsNames;
	}
	
	public static void registerCustomNodeTypes(Session session, String cndPath) throws IOException, RepositoryException, ParseException {
		if (cndPath==null || cndPath.isEmpty()) {
			log.error("CND path is null or empty." );
			return;
		}
		NamespaceRegistry registry = session.getWorkspace().getNamespaceRegistry();
		List<String> prefixes=Arrays.asList(registry.getPrefixes());
		if (!prefixes.contains("fsp"))
			registry.registerNamespace("fsp", "http://www.findlaw.com/jcr/fsp");
		if (!prefixes.contains("prop"))
			registry.registerNamespace("prop", "http://www.findlaw.com/jcr/fsp/prop");
		CndImporter.registerNodeTypes(new FileReader(cndPath), session);
	}

}
