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

import javax.jcr.NodeIterator;
import javax.jcr.query.RowIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Querier {
	
	protected static Log log=LogFactory.getLog(Querier.class);
	
	public static NodeIterator queryBySQLNode(Session session, String query) throws RepositoryException {
		QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery(query, Query.JCR_SQL2);
        QueryResult result = q.execute();
        NodeIterator it = result.getNodes();
        return it;
	}
	
	public static RowIterator queryBySQLRow(Session session, String query) throws RepositoryException {
		QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery(query, Query.JCR_SQL2);
        QueryResult result = q.execute();
        RowIterator it = result.getRows();
        return it;
	}
	
	public static RowIterator queryBySQLRow(Session session, String query, String queryType) throws RepositoryException {
		if (queryType==null || queryType.isEmpty()) queryType=Query.JCR_SQL2;
		QueryManager qm = session.getWorkspace().getQueryManager();
        Query q = qm.createQuery(query, queryType);
        QueryResult result = q.execute();
        RowIterator it = result.getRows();
        return it;
	}

}
