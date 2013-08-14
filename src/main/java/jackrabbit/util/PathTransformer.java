package jackrabbit.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;

public class PathTransformer {
	
	/**
	 * repo directory path
	 * @param repoPath
	 * @return
	 */
	public static String[] transform(String repoPath) {
		if (repoPath.isEmpty())
			return new String[] {};
		String parent="";
		if (repoPath.endsWith("/*")) {
			parent=repoPath.substring(0, Math.max(1, repoPath.length()-2));
		} 
		if (parent.isEmpty())
			return new String[]{repoPath};
		File dir=new File(parent);
		if (!dir.exists()) 
			return new String[] {};
		List<String> files=new ArrayList<String>();
		for (String f:dir.list(FileFilterUtils.directoryFileFilter())) {
			files.add(parent+"/"+f);
		}
		return files.toArray(new String[0]);
	}
	
	public static String[] transform(String repoPath, FilenameFilter filter) {
		File dir=new File(repoPath);
		if (!dir.exists()) 
			return new String[] {};
		if (filter==null)
			return new String[]{repoPath};
		return dir.list(filter);
	}
	
	/**
	 * Modify node path with regular expression
	 * @param nodePath
	 * @param regex
	 * @param replacement
	 * @return
	 */
	public static String transform(String nodePath, String regex, String replacement) {
		return nodePath.replaceAll(regex, replacement);
	}
	
	/**
	 * Modify node path with a user-defined modifier
	 * @param nodePath
	 * @param modifier
	 * @return
	 */
	public static String transform(String nodePath, NodePathModifier modifier) {
		return modifier.modify(nodePath);
	}
	


}
