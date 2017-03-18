---
layout: docs
title: Testing
description: Details about how to test benchflow
group: developer-guide
---

# How to Test BenchFlow


## Testing Artefacts

TODO - why some are shared, and the use (test + docs) we do of them

### Project Specific Artefacts

### Shared Test Artefacts

Some of the test artefacts, especially data, are shared among different benchflow projects. 
We place these artefacts in a `tests` folder in the [root of the BenchFlow project](https://github.com/benchflow/benchflow), organized in subfolders according to the type of the stored artefacts. 

The subfolders we use are:

- `data`: containing test data organised in folders according to the type of data. 