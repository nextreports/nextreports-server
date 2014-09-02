<!-- I cannot use jdk 1.6 in buildhive
Current build status: [![Build Status](https://buildhive.cloudbees.com/job/nextreports/job/nextreports-server/badge/icon)](https://buildhive.cloudbees.com/job/nextreports/job/nextreports-server/)
-->
Current build status: [![Build Status](https://travis-ci.org/nextreports/nextreports-server.png?branch=master)](https://travis-ci.org/nextreports/nextreports-server)

For more information about NextReports Server see the product page [link](http://www.next-reports.com/index.php/products/nextreports-server.html).

How to build
-------------------
Requirements:
- [Git](http://git-scm.com/)
- JDK 1.7 (test with `java -version`)
- [Apache Ant](http://ant.apache.org/) (test with `ant -version`)

Steps:
- create a local clone of this repository (with `git clone https://github.com/nextreports/nextreports-server.git`)
- go to project's folder (with `cd nextreports-server`)
- build the artifacts (with `ant clean release`)

After above steps a folder _artifacts_ is created and all goodies are in that folder.

How to run
-------------------
It's very simple to run the nextreports-server.
First, you must build the project using above steps.
After building process go to _dist_ folder and replace some variables:
- `@httpPort@` in ./etc/jetty.xml (for example you can replace this variable with _8081_)
- `@reportsHome@` in ./contexts/reports.xml (for example you can replace this variable with _reports_)

Execute the script:
- run.bat (for windows)
- run.sh (for linux/unix)

Start an internet browser immediately after the server starts (it displays in command prompt something like "Started SelectChannelConnector@0.0.0.0:8081") and type `http://localhost:@httpPort@/nextreports-server` (for example http://localhost:8081/nextreports-server).
In login page enter the default username and password: __admin__ as username and __1__ as password.

Using Maven
-------------------
NextReports Server comes with some web services that allows other applications to interact with the server using a simple API on following levels:
- storage level: list reports, charts, folders, data sources; create folders; publish reports, charts and data sources; download reports and charts (these methods are also used by NextReports Designer)
- process level: run reports on the server from your proprietary applications with your specific parameters values

In your pom.xml you must define the dependencies to nextreports-server-clients artifacts with:

```xml
<dependency>
    <groupId>ro.nextreports</groupId>
    <artifactId>nextreports-server-client</artifactId>
    <version>${nextreports-server-client.version}</version>
</dependency>
```

where ${nextreports-server-client.version} is the last nextreports-server-client version.

You may want to check for the latest released version using [Maven Search](http://search.maven.org/#search%7Cga%7C1%7Cnextreports-server-client)
