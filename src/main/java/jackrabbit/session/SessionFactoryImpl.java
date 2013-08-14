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

package jackrabbit.session;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SessionFactoryImpl implements SessionFactory {
	
	protected static final Log log = LogFactory.getLog(SessionFactory.class);
 	private Repository repository;
 	private Credentials credentials;
 	
 	public SessionFactoryImpl(Repository repository, Credentials credentials) {
 		this.repository = repository;
 		this.credentials = credentials;
 	}

	public Session getSession() throws RepositoryException {
		return repository.login(credentials);
	}
	
	public Session getSession(String workspaceName) throws RepositoryException {
		return repository.login(credentials, workspaceName);
	}

	public Repository getRepository() {
		return repository;
	}

	public Credentials getCredentials() {
		return credentials;
	}
	

}
