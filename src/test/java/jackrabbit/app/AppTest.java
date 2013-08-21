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

import java.util.List;

import jackrabbit.repository.RepositoryFactory;
import jackrabbit.repository.RepositoryFactoryImpl;
import jackrabbit.repository.RepositoryManager;
import jackrabbit.session.SessionFactory;
import jackrabbit.session.SessionFactoryImpl;
import jackrabbit.tool.NodeCopier;
import jackrabbit.util.NodePathModifier;
import jackrabbit.util.NodeUtils;
import jackrabbit.util.PathTransformer;
import jackrabbit.util.RegexModifier;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import org.junit.Before;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
	
	protected static Log log=LogFactory.getLog(AppTest.class);
	
	//change the following paths as appropriate
	private static final String REPO_ROOT="/root/sites";
	private static final String REPO_PATH="/repository";
	private final String srcRepoDir=REPO_PATH+"/folder";
	private final String srcRepoPath=REPO_ROOT+"/folder";
	private final String destRepoDir=REPO_PATH+"/all-mysql-1";
	private final String srcConf="config/repository_derby.xml";
	private final String destConf="config/repository-mysql-1.xml";
	private final String cndPath="config/base_nodetypes.cnd";
	
	private JackrabbitRepository src;
	private JackrabbitRepository dest;
	private SessionFactory srcSf;
	private SessionFactory destSf;
	private Session srcSession;
	private Session destSession;

	@Before
	public void setUp() throws Exception {
		RepositoryFactory srcRf=new RepositoryFactoryImpl(srcConf, srcRepoDir);
    	RepositoryFactory destRf=new RepositoryFactoryImpl(destConf, destRepoDir);
    	try {
	    	src=srcRf.getRepository();
    	} catch (AssertionError e){
    		//if the src repository was indexed using pre-2.4.1 Lucene, an assert error about del count mismatch may occur (https://issues.apache.org/jira/browse/LUCENE-1474).
    		log.error(e.getMessage(), e);
    	}	    	
    	dest=destRf.getRepository();
    	SimpleCredentials credentials=new SimpleCredentials("username", "password".toCharArray());
    	if (src==null)
    		return;
    	srcSf=new SessionFactoryImpl(src, credentials);
    	destSf=new SessionFactoryImpl(dest, credentials);
    	srcSession=srcSf.getSession();
    	destSession=destSf.getSession();
    	RepositoryManager.registerCustomNodeTypes(destSession, cndPath);
    	
	}
	
	@Test
    public void testApp() throws Exception {
    	if (src==null)
    		return;
    	NodePathModifier modifier=new RegexModifier("(.*)\\/(\\w)(\\w+)$", "$1/$2");
		String destRepoPath=PathTransformer.transform(srcRepoPath, modifier);
		NodeCopier.copy(srcSession, destSession, srcRepoPath, destRepoPath, true);	
		List<String> destWkspaces=RepositoryManager.getDestinationWorkspaces(srcSession, destSession);
		for (String workspace:destWkspaces) {
    		Session wsSession=srcSf.getSession(workspace);
    		Session wsDestSession=destSf.getSession(workspace);
    		RepositoryManager.registerCustomNodeTypes(wsDestSession, cndPath);
			NodeCopier.copy(wsSession, wsDestSession, srcRepoPath, destRepoPath, true);
			wsSession.logout();
			wsDestSession.logout();
		}	
		NodeUtils.explore(destSession.getNode("/fsp_root/sites/a/aeellp"));
    }
	
	@Test
    public void testAppWithNodeLimit() throws Exception {
    	if (src==null)
    		return;
    	NodePathModifier modifier=new RegexModifier("(.*)\\/(\\w)(\\w+)$", "$1/$2");
		String destRepoPath=PathTransformer.transform(srcRepoPath, modifier);
		NodeCopier.copy(srcSession, destSession, srcRepoPath, destRepoPath, 10000, true);
		List<String> destWkspaces=RepositoryManager.getDestinationWorkspaces(srcSession, destSession);
		for (String workspace:destWkspaces) {
    		Session wsSession=srcSf.getSession(workspace);
    		Session wsDestSession=destSf.getSession(workspace);
    		RepositoryManager.registerCustomNodeTypes(wsDestSession, cndPath);
			NodeCopier.copy(wsSession, wsDestSession, srcRepoPath, destRepoPath, 10000, true);
			wsSession.logout();
			wsDestSession.logout();
		}	
		NodeUtils.explore(destSession.getNode("/fsp_root/sites/a/aeellp"));
    }
	
		
	@After 
	public void tearDown() throws AccessDeniedException, VersionException, LockException, ConstraintViolationException, PathNotFoundException, RepositoryException {
		if (srcSession!=null)
			srcSession.logout();
		if (src!=null)
			src.shutdown();
		if (destSession!=null)
			destSession.logout();
		if (dest!=null)
			dest.shutdown();		
	}
}
