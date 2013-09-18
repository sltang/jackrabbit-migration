<h2>Apache Jackrabbit Migration and Query Tool</h2>
We present a backup/migration tool for Apache Jackrabbit complementary to the existing ones (<a href="http://wiki.apache.org/jackrabbit/BackupAndMigration">http://wiki.apache.org/jackrabbit/BackupAndMigration</a>). It provides some 
customization features that make it easier to use. See <a href="http://github.com/sltang/jackrabbit-migration/wiki">wiki</a> for more information.

<h3>Usage</h3>
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


