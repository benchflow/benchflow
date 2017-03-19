## Design

### Architecture

See documentation/architecture.jpg


### Command line interface

```
data-manager backup data_ID
data-manager list backup [data_ID]
data-manager restore [latest | timestamp] data_ID
data-manager status
```

### Control flow

- check if we can backup the data (we need to extend and provide the APIs from the `experiments-manager` and the `spark-tasks-sender`)
- mark the data as unavailable (talk to the experiments-manager service)
- get data sources from data source provider
- for each data source
    fetch data and store in backup storage
- marks the data as available

We are going to use actors and streams to move data from the data sources to the backup storage and back


### Data sources

The data sources currently are:
Database, Cassandra, Minio (similar to [Amazon S3](https://aws.amazon.com/s3/)), MemSQL

To add a new data source the options seem to be:
  - Implement trait and add implementation to some list in the source code
  - Implement trait and "link" it to the data manager using a plug-in system (e.g. https://github.com/decebals/pf4j)
  - Use `git-annex` or `git-lts`.


### Backup storage

For now we are going to use Google Drive but the code should be flexible enough to accommodate other kinds of backup storage in the future.
(it is important to remember the different backup storage "solutions" will have different APIs, different ways to authenticate and etc.)

Where to store data in the file system?
`/Project_root_folder/data_ID/timestamp/data_source_name`

Timestamp may not be necessary if using versioning system (e.g. git-annex)

Metadata should be stored in human readable format together with the data so the system is able to restore it.


### Other Questions

#### Versioning of large files?
Look into git-annex, git-lfs (others: http://blog.deveo.com/storing-large-binary-files-in-git-repositories/)

**_Requirements to select the right solution for us:_** multiple data storage, versioning using git, Java/Scala library, streaming data to and back from the data storage (no need of big temp files in the service)

