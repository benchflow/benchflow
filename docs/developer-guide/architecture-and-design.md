---
layout: docs
title: Architecture and Design
description: BenchFlow Architecture and Design
group: developer-guide
---

<!-- Add dynamic load of images, and if necessary split on multiple pages -->

## Overview

<!--  -->

<!-- Common content: Entities Diagram, Interaction Diagram, APIs with Swagger -->

## BenchFlow Client

<!--  -->

## BenchFlow DSL

### Serializing/Deserializing YAML
To serialize/deserialize YAML the DSL library uses [MoultingYAML](https://github.com/jcazevedo/moultingyaml).

### Naming of `*YamlProtocol` files and case classes
Most of the `*YamlProtocol` files can be shared for both Tests and Experiments, as well as their case class counterparts. In the cases where we need to separate the protocols we different the files by prepending `BenchFlowTest` and `BenchFlowExperiment` for tests and experiments respectively, e.g. `BenchFlowExperimentConfigurationYamlProtocol` and `BenchFlowExperimentConfiguration`. When there is no need to make this separation, i.e., the files are shared between test and experiment, we should not use the prefix in the filename.

<!--  -->
