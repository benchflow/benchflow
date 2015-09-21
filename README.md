# BenchFlow
BenchFlow is an open-source framework that provides a complete platform for executing performance benchmarks and performance tests. Its functionalities include:

1. definition of a performance benchmark/test through a dedicated DSL;
2. automatisation of the deployment of the System Under Test on distributed infrastructures using [Docker](https://www.docker.com);
3. reliable execution of the performance benchmark using [Faban](http://faban.org);
4. data collection and cleaning;
5. data analysis in the form of computed metrics and KPIs.

*Current Focus of the Framework*: 
The BenchFlow framework is currently mainly focused on enabling the performance benchmark of Workflow Management Systems supporting the BPMN 2.0 modeling and execution language. Despite the main focus, most of its components are reusable and already general enough to support performance benchmarks of generic Web Services. We strongly encourage extending BenchFlow by adding missing functionalities specific to your particular benchmarking needs. 

*Website related to the current focus*: http://benchflow.inf.usi.ch

# About this repository
This is a documentation and meta-repository. Follow the links you can find on this README file to read the documentation and to explore all the sub-projects of the BenchFlow framework.

# Project Description
TODO: describe the main components and the overall architecture of the system on dedicated md files, pointed by a brief summary to be presented here.

# Project Status
The project is currently in active development and is tested on Mac OS X for the client command line side tools, and Ubuntu 14.04.2 LTS for the server side tools. 

***WARNING:***We are currently releasing all the Sub-projects of the BenchFlow framework in their current status.

Our current main branch is: dev

# Project DevOps Lifecycle
*Dev*: we use [Eclipse](https://www.eclipse.org/home/index.php) to develop BenchFlow. Each repository has one or more Eclipse projects if needed.
TODO: Describe the DevOps automated lifecycle

# Dependencies
TODO: describe the main dependencies of the project

# Installation
TODO

# Requirements
TODO

# How to Use BenchFlow
TODO

# Sub-projects
TODO: point to the sub-projects briefly introducing them

# How to Contribute
[Contributing](documentation/contributing.md)

# Roadmap
TODO (Example from: https://github.com/docker/docker/blob/master/ROADMAP.md)

# TODOs
* Fill all the TODOs in the README.md
* Improve the contribution guidelines by following the Docker approach: https://github.com/docker/docker/blob/master/CONTRIBUTING.md
* TODOs: Each of the sub-project has its own TODOs list 

# Support, Discussion, and Community
TODO

# License
The license for all the code in the BenchFlow repositories is [RPL-1.5](LICENSE), unless otherwise noted