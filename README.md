<!-- I cannot use jdk 1.6 in buildhive
Current build status: [![Build Status](https://buildhive.cloudbees.com/job/nextreports/job/nextreports-server/badge/icon)](https://buildhive.cloudbees.com/job/nextreports/job/nextreports-server/)
-->
Current build status: [![Build Status](https://travis-ci.org/nextreports/nextreports-server.png?branch=master)](https://travis-ci.org/nextreports/nextreports-server)

How to build
-------------------
Requirements: 
- [Git](http://git-scm.com/) 
- JDK 1.6 (test with `java -version`)
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

