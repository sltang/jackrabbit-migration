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

package jackrabbit.app;

import java.io.IOException;
import java.util.List;

import jackrabbit.query.Querier;
import jackrabbit.repository.RepositoryFactory;
import jackrabbit.repository.RepositoryFactoryImpl;
import jackrabbit.repository.RepositoryManager;
import jackrabbit.session.SessionFactory;
import jackrabbit.session.SessionFactoryImpl;
import jackrabbit.tool.NodeCopier;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.core.RepositoryImpl;

public class App {
	
	protected static Log log=LogFactory.getLog(App.class);
		
	private static String srcRepoDir="";
	private static String srcRepoPath="/";
	private static String destRepoDir="";
	private static String destRepoPath="/";
	private static String srcConf="";
	private static String destConf="";
	private static String cndPath="";
	private static String query="";
	private static String queryType="";
	
	
    public static void main(String[] args) {
    	if (args.length == 0 || args.length == 1 && args[0].equals("-h")) {
    		System.out.println("Usage: java -jar jackrabbit-migration-query-tool-1.0.0-jar-with-dependencies.jar --src src --src-conf conf [--src-repo-path path] [--dest dest] [--dest-conf conf] [--dest-repo-path path] [--cnd cnd] [--query query] [--query-type type]");
    		System.out.println("\t --src source repository directory");
    		System.out.println("\t --src-conf source repository configuration file");
    		System.out.println("\t --src-repo-path path to source node to copy from; default is \"/\"");
    		System.out.println("\t --dest destination repository directory");    		
    		System.out.println("\t --dest-conf source repository configuration file");    		
    		System.out.println("\t --dest-repo-path path to destination node to copy to; default is \"/\"");
    		System.out.println("\t --cnd common node type definition file");
    		System.out.println("\t --query query to run in src. If --query is specified, then --dest, --dest-conf, --dest-repo-path and --cnd will be ignored.");
    		System.out.println("\t --query-type query type (sql, xpath, JCR-SQL2); default is JCR-SQL2");
    		return;
    	}
    	for (int i=0;i<args.length;i=i+2) {
    		if (args[i].equals("--src") && i+1<args.length) {
    			srcRepoDir=args[i+1];
    		} else if (args[i].equals("--dest") && i+1<args.length) {
    			destRepoDir=args[i+1];
    		} else if (args[i].equals("--src-conf") && i+1<args.length) {
    			srcConf=args[i+1];
    		} else if (args[i].equals("--dest-conf") && i+1<args.length) {
    			destConf=args[i+1];
    		} else if (args[i].equals("--src-repo-path") && i+1<args.length) {
    			srcRepoPath=args[i+1];
    		} else if (args[i].equals("--dest-repo-path") && i+1<args.length) {
    			destRepoPath=args[i+1];
    		} else if (args[i].equals("--cnd") && i+1<args.length) {
    			cndPath=args[i+1];
    		}  else if (args[i].equals("--query") && i+1<args.length) {
    			query=args[i+1];
    		} else if (args[i].equals("--query-type") && i+1<args.length) {
    			queryType=args[i+1];
    		}
    	}
    	boolean missingArgs=false;
    	boolean isQueryMode=!query.isEmpty();
    	if (srcRepoDir.isEmpty()) {
    		missingArgs=true;
    		log.error("Please specify the --src option.");
    	}
    	if (destRepoDir.isEmpty() && !isQueryMode) {
    		missingArgs=true;
    		log.error("Please specify the --dest option.");
    	}
    	if (srcConf.isEmpty()) {
    		missingArgs=true;
    		log.error("Please specify the --src-conf option.");
    	}
    	if (destConf.isEmpty() && !isQueryMode) {
    		missingArgs=true;
    		log.error("Please specify the --dest-conf option.");
    	}
    	
    	if (missingArgs) return;
    	    	
    	SimpleCredentials credentials=new SimpleCredentials("username", "password".toCharArray());
    	    	
    	RepositoryImpl src=null;
    	RepositoryImpl dest=null;  	
    	RepositoryFactory destRf=null;
    	
		RepositoryFactory srcRf=new RepositoryFactoryImpl(srcConf, srcRepoDir);
		if (!destConf.isEmpty())
			destRf=new RepositoryFactoryImpl(destConf, destRepoDir);
    	        	
    	try {
	    	src=srcRf.getRepository();
	    	SessionFactory srcSf=new SessionFactoryImpl(src, credentials);
	    	Session srcSession=srcSf.getSession();
	    	    	    	
	    	if (isQueryMode) {
	    		RowIterator rowIt=Querier.queryBySQLRow(srcSession, query, queryType);
	    		while (rowIt.hasNext()) {
	            	Row row = rowIt.nextRow();
	            	Value[] values=row.getValues();
	            	String s="";
	            	for (Value value:values) {
	            		s+="|"+value.getString();
	            	}
	            	System.out.println(s);
	    		}
	    		srcSession.logout();
	    		src.shutdown();
	    		return;
	    	}
	    	
	    	dest=destRf.getRepository();
	    	SessionFactory destSf=new SessionFactoryImpl(dest, credentials); 
	    	Session destSession=destSf.getSession();
	    	
	    	try {
	    		RepositoryManager.registerCustomNodeTypes(destSession, cndPath);
				NodeCopier.copy(srcSession, destSession, srcRepoPath, destRepoPath, true);
				log.info("Copying "+srcSession.getWorkspace().getName()+" to "+destSession.getWorkspace().getName()+ " for "+srcRepoDir + " done.");
	    	} catch (ParseException e) {
				log.error(e.getMessage(), e);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			} catch (PathNotFoundException e) {
				log.error(e.getMessage(), e);
			} catch (RepositoryException e) {
				log.error(e.getMessage(), e);
			} finally {
				srcSession.logout();
		    	destSession.logout();
			}
	
	    	List<String> destWkspaces=RepositoryManager.getDestinationWorkspaces(srcSession, destSession);
	    	
	    	for (String workspace:destWkspaces) {
	    		Session wsSession=srcSf.getSession(workspace);
	    		Session wsDestSession=destSf.getSession(workspace);
	    		try {
	    			RepositoryManager.registerCustomNodeTypes(wsDestSession, cndPath);
	    			NodeCopier.copy(wsSession, wsDestSession, srcRepoPath, destRepoPath, true);
	    			log.info("Copying "+wsSession.getWorkspace().getName()+" to "+wsDestSession.getWorkspace().getName()+ " for " + srcRepoDir + " done.");
	    		} catch (IOException e) {
	    			log.error(e.getMessage(), e);
	    		} catch (ParseException e) {
	    			log.error(e.getMessage(), e);
	    		} catch (PathNotFoundException e) {
	    			log.error(e.getMessage(), e);
	    		} catch (RepositoryException e) {
	    			log.error(e.getMessage(), e);
	    		} finally {
	    			wsSession.logout();
	    			wsDestSession.logout();
	    		}
	    	}
    	} catch (IOException e) {
    		log.error(e.getMessage(), e);
		} catch (PathNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (src!=null) src.shutdown();
			if (dest!=null) dest.shutdown();
		}
    }
}
