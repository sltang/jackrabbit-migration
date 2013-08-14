<h1>Goals</h1>
<p>While the migration/backup tools provided by Jackrabbit are enough in standard cases (http://wiki.apache.org/jackrabbit/BackupAndMigration), there are situations where further customizations are needed.
For instance, you may want to consolidate a number of repositories into a single one for ease of reporting or for clustering purposes. There are some performance issues if a node has too
many child nodes. Using this tool, you can specify the node path when you copy a repository into another to make more
hierarchical to avoid having too many child nodes under a node. This tool can be run in the command line to copy one repository to another or as a subnode of another repository.
This tool also provides some querying capability. You can pass in a query string to run a query against a specific repository.
</p>
<p>The tool can also be used as a library in your application. As such, there is more refined control over the repository source(s).</p>
<h1>Usage</h1>
<pre>
$java -jar jackrabbit-migration-query-1.0.0-jar-with-dependencies.jar
Usage: java -jar jackrabbit-migration-query-tool-1.0.0-jar-with-dependencies.jar --src src --src-conf conf [--src-repo-path path] [--dest dest] [--dest-conf conf] 
[--dest-repo-path path] [--cnd cnd] [--query query] [--query-type type]
         --src source repository directory
         --src-conf source repository configuration file
         --src-repo-path path to source node to copy from; default is "/"
         --dest destination repository directory
         --dest-conf source repository configuration file
         --dest-repo-path path to destination node to copy to; default is "/"
         --cnd common node type definition file
         --query JCR-SQL2 query to run in src. If --query is specified, then --dest, --dest-conf, --dest-repo-path and --cnd will be ignored.
         --query-type query type (SQL, XPATH, JCR-SQL2); default is JCR-SQL2"
</pre>

       
<h1>Requirements</h1>
<ul>
<li>JDK 1.6 or above.</li>
<li>Maven 2.2.1 or above.</li>
</ul>

<h1>Build</h1> 
<p>You can package it as a jar and include it as part of your application. Run<p> 

<p>mvn packge</p>

<p>You can package it include all the dependencies so the jar can be executed by itself. Run</p>

<p>mvn packge -Papplication</p>


<h1>Jackrabbit Repository Configuration Files</h1>
<p>We have included some basic Jackrabbit repository configuration files in the config directory. They include using Derby or MySQL as database storage. You can further customize those files
to suit your own needs.</p>

<p>Comments and suggestions are welcomed.</p>

<h1>Known issue</h1>
<p>
During development, we discovered an issue when using MySQL as the backend database (on Windows). When importing a large XML into a repository, it failed with the following error:
</p>
<p>
ERROR org.apache.jackrabbit.core.cluster.ClusterNode$WorkspaceUpdateChannel - Unexpected error while committing log entry.
java.lang.RuntimeException: Unable to reset the Stream.
	at org.apache.jackrabbit.core.util.db.ConnectionHelper.execute(ConnectionHelper.java:525)
    ...
</p>
<p>   
It turns out that the default value of max_allowed_packet value (on Windows) is 1M. So if you are working with large repositories, you may run into the above error. To fix it, set 
max_allowed_packet to a large value in [mysqld] in my.ini.
</p>