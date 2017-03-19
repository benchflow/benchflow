# Requirements

The BenchFlow data-manager handles the BenchFlow data.
Its main responsibilities are:

1. Backup and Restore data to cheaper storages (e.g., Google Drive)
2. Perform versioned backups of the data
3. Access to data on demand (because it knows every data storage and the data state in the lifecycle)


## Design requirements

- The system should be flexible enough to allow the user to add new data sources
  with as little changes as possible

