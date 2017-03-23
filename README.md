# BenchFlow

BenchFlow is an open-source expert system providing a complete platform for automating performance tests and performance analysis. We know that not all the developers are performance experts, but in nowadays agile environment, they need to deal with performance testing and performance analysis every day. In BenchFlow, the users define objective-driven performance testing using an expressive and SUT-aware DSL implemented in [YAML](http://www.yaml.org). Then BenchFlow automates the end-to-end process of executing the performance tests and providing performance insights, dealing with system under test deployment relying on [Docker](https://github.com/docker/docker) technologies, distributing simulated users load on different server, error handling, performance data collection and performance metrics and insights computation. 

Quick links: [BenchFlow Documentation]() | [TODO - also link to the documentation]()

TODO (try BenchFlow)

## Purpose

TODO (BenchFlow has a strong focus on developer happiness & ease of use, and a batteries-included philosophy.)

#### Current project focus

The BenchFlow expert system is currently mainly focused on enabling the performance benchmark of [Workflow Management Systems supporting the BPMN 2.0 modeling and execution language](https://en.wikipedia.org/wiki/List_of_BPMN_2.0_engines). Despite the main focus, most of its components are reusable and already general enough to support performance benchmarks of generic Web Services. We strongly encourage extending BenchFlow by adding missing functionalities specific to your particular benchmarking needs.
TODO ([point to setup and getting started]). Website related to the current focus: [http://benchflow.inf.usi.ch](http://benchflow.inf.usi.ch). 

We have a temporary logo, we are going to have a proper logo at some point in the future. 

#### Upcoming project focus

(TODO) automated objective-driven performance testing, and integration in continuous software improvement lifecycle. 

## Features (Why BenchFlow?)

TODO (also link to the documentation)

definition of a performance benchmark/test through a dedicated DSL;
automatisation of the deployment of the System Under Test on distributed infrastructures using Docker;
reliable execution of the performance benchmark using Faban;
data collection and cleaning;
data analysis in the form of computed metrics and KPIs.

## Use Cases

TODO (to show the uses of the tool, linking to an actual article explaining how to do that, point also to controbuting for extending)

## Installation or Upgrade

TODO (explain current project state in dev, and link to docs and uses to state that it is usable, but not 100% battle tested)

TODO (getbenchflow in container for client and docs for the rest [also links to docker hub if needed, ot least in the developer documentation], current release. Explain: Docker as prerequisite)

(TODO, maybe at the top) Project Status: The project is currently in active development and is tested on Mac OS X for the client command line side tools, and Ubuntu 14.04.2 LTS for the server side tools. The main project branch is `devel` [maybe for now say that there are no release yet, but we are in the process of having the first release]

## Getting Started

### Prerequisites

TODO (simplest example, then links to the docs for advanced stuff)

### Installing

TODO (needs help or customisation, write contacts)

## Built With

TODO

## Contributing

TODO (also related to extending to custom software, and links to developer documentation and TODOs)

## Versioning

TODO (SemVer + link to docs)

## Authors

TODO

## License

Copyright © 2014-2017, [Vincenzo Ferme](http://www.vincenzoferme.it) for own and contributors committed code and artefacts. 

The license for all the not third-party code in the BenchFlow repositories is [RPL-1.5](LICENSE), unless otherwise noted
