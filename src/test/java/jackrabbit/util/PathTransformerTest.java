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

import static org.junit.Assert.*;

import java.io.File;

import jackrabbit.util.PathTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class PathTransformerTest {
	
	protected static Log log=LogFactory.getLog(PathTransformerTest.class);

	@Test
	public void testTransformPath() {
		String path="/tmp/jackrabbit-test";
		
		new File(path+"/test1").mkdirs();
		new File(path+"/test2").mkdirs();
		new File(path+"/test3").mkdirs();

		String[] dirs=PathTransformer.transform(path);
		assertTrue(dirs.length==1);
		
		String[] dirs2=PathTransformer.transform(path+"/*");
		assertTrue(dirs2.length==3);
		
		removeDirectory(new File(path));		
	}

	private void removeDirectory(File dir) {
	    if (dir.isDirectory()) {
	        File[] files = dir.listFiles();
	        if (files != null && files.length > 0) {
	            for (File file : files) {
	                removeDirectory(file);
	            }
	        }
	        dir.delete();
	    } else {
	        dir.delete();
	    }
	}
	
	@Test
	public void testTransformNodePath() {
		String nodePath="/a/b/cd";
		String transPath=PathTransformer.transform(nodePath, "(.*)\\/(\\w)(\\w*)$", "$1/$2/$2$3");
		assertEquals(transPath, "/a/b/c/cd");
		
		NodePathModifier modifier=new RegexModifier("(.*)\\/(\\w)(\\w+)$", "$1/$2");
		String srcRepoDir="/tmp/*";
    	String[] srcRepoDirs=PathTransformer.transform(srcRepoDir);
    	for (String srcRepoSubDir:srcRepoDirs) {
    		log.info("srcRepoSubDir: "+srcRepoSubDir);
    		String[] paths=srcRepoSubDir.split("/");
    		if (paths.length==0) continue;
    		String srcRepoPath="/root/"+paths[paths.length-1];
    		String destRepoPath=PathTransformer.transform(srcRepoPath, modifier);
    		log.info("srcRepoPath: "+srcRepoPath);
    		log.info("destRepoPath :"+destRepoPath);
    	}
	}
}
