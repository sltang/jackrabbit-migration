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

public class RegexModifier implements NodePathModifier{
	
	private String regex;
	private String replacement;
	
	public RegexModifier(String regex, String replacement) {
		this.regex=regex;
		this.replacement=replacement;
	}

	public String modify(String path) {
		return path.replaceAll(regex, replacement);
	}
	
	public String modify(String path, boolean firstOnly) {
		if (firstOnly)
			return path.replaceFirst(regex, replacement);
		return path.replaceAll(regex, replacement);
	}

}
