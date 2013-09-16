<h2>Apache Jackrabbit Migration and Query Tool</h2>
<h3>Introduction</h3>
<p>Apach Jackarbbit is an open-source implementation of Java Content Repository (JCR). Its flexibility in data schema and
features have made it a popular choice for storing structured and unstructured contents.
While the migration/backup tools provided by Jackrabbit are sufficient in most cases (http://wiki.apache.org/jackrabbit/BackupAndMigration), there 
are situations where further customizations are needed.
For instance, you may want to consolidate a number of repositories into a single one for ease of reporting or for clustering purposes. The goals
of this project are to extend some of the existing features to cover more use cases.
Using the tool from this project, you can specify the node path when you copy a repository into another to make more hierarchical to avoid having too many child 
nodes under a single node. (There are some known performance issues if a node has too many child nodes.)
This tool can be run in the command line to copy one repository to another or as a subnode of another repository. 
You can also use this tool to run queries in SQL, XPATH or JCR-SQL2 against a repository. The tool can also be used as a standard library in your application. 
As such, one can exercise more refined control over the copying process.</p>
<h3>Improvements</h3>
<p>Our migration tool is based on the exportSystemView and importXML functions in JCR. We look at the case where the node or repository to be 
copied is too large to fit into memory when using those functions. First, if the node involved is too large, there may be out of memory errors.
Second, even if there is no memory issue, the underlying storage may impose restrictions on the size of data packets it receives. For instance, if 
MySQL is used as the backend database (on Windows), when importing a large XML into a repository, it may fail with the following error:</p>
<div class="highlight highlight-java">
<pre>
ERROR org.apache.jackrabbit.core.cluster.ClusterNode$WorkspaceUpdateChannel - Unexpected error while committing log entry.
java.lang.RuntimeException: Unable to reset the Stream.
	at org.apache.jackrabbit.core.util.db.ConnectionHelper.execute(ConnectionHelper.java:525)
    ...
</pre>
</div>
<p>  
It turns out that the default value (1MB) of MySQL's max_allowed_packet on Windows is too small. You can fix that by setting max_allowed_packet to a 
larger value in [mysqld] in my.ini.
</p>
<p>
We address the issue by partitioning the source node to subnodes of size under a specified value, and run export and import on 
each subnode separately.  Since references may exist between partitioned subnodes, you may run into some ItemNotFoundExceptions and ConstraintViolationExceptions 
when importing. It is permissible to suppress those errors during import as references will be satisfied after the all the subnodes have been imported. 
With a suitable choice of the subnode size, we can import and export without altering the max_allowed_packet value.
</p>
<h3>Usage</h3>
<div class="highlight highlight-java">
<pre>
$java -jar jackrabbit-migration-query-${version.number}-jar-with-dependencies.jar
Usage: java -jar jackrabbit-migration-query-${version.number}-jar-with-dependencies.jar --src src --src-conf conf [--src-repo-path path] [--dest dest] [--dest-conf conf] 
[--dest-repo-path path] [--cnd cnd] [--node-limit limit] [--query query] [--query-type type]
         --src source repository directory
         --src-conf source repository configuration file
         --src-repo-path path to source node to copy from; default is /
         --dest destination repository directory
         --dest-conf source repository configuration file
         --dest-repo-path path to destination node to copy to; default is /
         --node-limit size (in bytes) to partition nodes with before copying. If it is not supplied, no partitioning is performed
         --cnd common node type definition file
         --query JCR-SQL2 query to run in src. If --query is specified, then --dest, --dest-conf, --dest-repo-path and --cnd will be ignored.
         --query-type query type (SQL, XPATH, JCR-SQL2); default is JCR-SQL2"
</pre>
</div>
If only --src and --src-conf (and optionally --query-type) are specified, it runs in query mode where queries can be entered one at a time.
       
<h3>Requirements</h3>
<ul>
<li>JDK 1.6 or above.</li>
<li>Maven 2.2.1 or above.</li>
</ul>

<h3>Build</h3> 
You need Maven and package it as a jar to include it as part of your application:

mvn package

You can include all the dependencies in the jar and make it executable:

mvn package -Papplication

It produces two jars. The jar-with-dependencies.jar contains all the dependencies. You can modify pom.xml to include other 
dependencies as needed. To run the unit tests, please update the paths in the tests as appropriate or skip the tests when packaging.


<h3>Jackrabbit Repository Configuration Files</h3>
<p>We have included some basic Jackrabbit repository configuration files in the config directory. They include using Derby or MySQL as database storage. You can further customize those files
to suit your own needs.</p>

<p>Any comments and suggestions are welcome.</p>

