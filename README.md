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
