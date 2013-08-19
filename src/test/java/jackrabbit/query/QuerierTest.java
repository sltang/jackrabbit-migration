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

package jackrabbit.query;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import jackrabbit.repository.RepositoryFactory;
import jackrabbit.repository.RepositoryFactoryImpl;
import jackrabbit.session.SessionFactory;
import jackrabbit.session.SessionFactoryImpl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class QuerierTest {
		
	protected static Log log=LogFactory.getLog(QuerierTest.class);
	
	//change as needed
	private final String repoDir="/repository";
	private final String repoConf="config/repository-mysql-1.xml";
	
	private RepositoryImpl repo;
	private Session session;

	
	@Before
	public void setUp() throws RepositoryException {
		RepositoryFactory srcRf=new RepositoryFactoryImpl(repoConf, repoDir);
		repo=srcRf.getRepository();
		SimpleCredentials credentials=new SimpleCredentials("username", "password".toCharArray());
		SessionFactory sessionFactory=new SessionFactoryImpl(repo, credentials);
		session=sessionFactory.getSession();
	}
	
	@Test
	public void testQueryByPropertyNode() throws Exception {
		NodeIterator it=Querier.queryBySQLNode(session, "select * from [ns:child] as child where ISDESCENDANTNODE(child, [/root/nodes/node]) and ([ns:property1]='value1' or [ns:property2]='value2')");
		while (it.hasNext()) {
        	Node node = it.nextNode();
        	log.info(node.getPath());
		}
	}
	
	@Test
	public void testQueryByPropertyRow() throws Exception {
		RowIterator it=Querier.queryBySQLRow(session, "select [ns:property] from [ns:child] as child where ISDESCENDANTNODE(child, [/root/nodes/node]) and ([ns:property1]='value1' or [ns:property2]='value2')");
		while (it.hasNext()) {
        	Row row = it.nextRow();
        	Value[] values=row.getValues();
        	String s="";
        	for (Value value:values) {
        		s+="|"+value.getString();
        	}
        	log.info(s);
		}
	}
	
	@Test
	public void testFullTextSearch() throws RepositoryException {
		NodeIterator contents=Querier.queryBySQLNode(session, "select * from [ns:content] as content where ISDESCENDANTNODE(content, [/root/nodes/node]) and contains(content.[ns:property], 'value1 +value2')");
		while (contents.hasNext()) {
        	Node node = contents.nextNode();
        	log.info(node.getPath());
        }
	}
	
	@After
	public void tearDown() {
		session.logout();
		repo.shutdown();
	}

}
