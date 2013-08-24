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

import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import jackrabbit.node.NodePartitioner;
import jackrabbit.node.NodeSizePartitioner;
import jackrabbit.repository.RepositoryFactory;
import jackrabbit.repository.RepositoryFactoryImpl;
import jackrabbit.session.SessionFactory;
import jackrabbit.session.SessionFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NodeUtilsTest {
	
	protected static Log log=LogFactory.getLog(NodeUtilsTest.class);
	
	//change as needed
	private final String repoPath="/root/nodes/node";
	private final String repoDir="/repository";
	private final String conf="config/repository-mysql-1.xml";
	
	private Session ss;
	private JackrabbitRepository repo;
	
	@Before 
	public void setUp() throws RepositoryException {
		RepositoryFactory rf=new RepositoryFactoryImpl(conf, repoDir);
		repo=rf.getRepository();
		SimpleCredentials credentials=new SimpleCredentials("username", "password".toCharArray());
		SessionFactory sf=new SessionFactoryImpl(repo, credentials);
		ss=sf.getSession();
	}
		
	@Test
	public void getPropertiesSize() throws Exception {
		long size=NodeUtils.getPropertiesSize(ss.getNode(repoPath));
		log.info("Node size of "+repoPath+ ": "+size);
	}
	
	@Test
	public void getDescendantsSize() throws PathNotFoundException, RepositoryException {
		long size=NodeUtils.getDescendantsSize(ss.getNode(repoPath));
		log.info("Descendents size of "+repoPath+ ": "+size);
	}
	
	@Test
	public void partition() throws PathNotFoundException, RepositoryException {
		Node node=ss.getNode(repoPath);
		long limit=100000;
		NodePartitioner partitioner=new NodeSizePartitioner(limit);
		Set<Map.Entry<String, Boolean>> descendants=partitioner.partition(node);
		for (Map.Entry<String, Boolean> entry: descendants) {
			log.info(entry.getKey()+" : "+entry.getValue());
		}		
	}
	
	
	@After
	public void tearDown() {
		ss.logout();
		repo.shutdown();
	}

}
