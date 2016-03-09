-- Drop the keyspace itf it exists
-- This means that currenlty an update of the image delete everything and create a new database.
--    - This is fine since we are testing and developing
DROP KEYSPACE IF EXISTS benchflow;

-- Create benchflow keyspace (currently we use only one node)
CREATE KEYSPACE benchflow
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

USE benchflow;

-- The experiment root table
CREATE TABLE experiment (
  experiment_id text,
  replication_num int,
  trial_id text,
  PRIMARY KEY ((trial_id, experiment_id))
);

-- Raw data
CREATE TABLE database_sizes (
  experiment_id text,
  trial_id text,
  dbms text,
  database_name text,
  size bigint,
  PRIMARY KEY ((trial_id, experiment_id), dbms)
);

CREATE TABLE io_data (
  io_data_id uuid,
  experiment_id text,
  trial_id text,
  container_id text,
  device text,
  writes bigint,
  reads bigint,
  sync bigint,
  async bigint,
  total bigint,
  PRIMARY KEY ((trial_id, experiment_id), container_id, io_data_id)
);

CREATE TABLE environment_data (
  environment_data_id uuid,
  container_id text,
  read_time text,
  cpu_percpu_usage list<bigint>,
  cpu_percpu_percent_usage list<double>,
  cpu_total_usage bigint,
  cpu_percent_usage double,
  cpu_throttling_data map<text, bigint>,
  memory_usage double,
  memory_max_usage double,
  network_interfaces map<text, uuid>,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id, experiment_id), container_id, environment_data_id)
);

CREATE TABLE network_interface_data (
  network_interface_data_id uuid,
  network_rx_dropped bigint,
  network_rx_bytes bigint,
  network_rx_errors bigint,
  network_tx_packets bigint,
  network_tx_dropped bigint,
  network_rx_packets bigint,
  network_tx_errors bigint,
  network_tx_bytes bigint,
  PRIMARY KEY (network_interface_data_id)
);

CREATE TABLE process (
  process_instance_id uuid,
  source_process_instance_id text,
  process_definition_id text,
  start_time timestamp,
  duration bigint,
  end_time timestamp,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id, experiment_id), process_instance_id)
);

CREATE TABLE construct (
  construct_instance_id uuid,
  source_construct_instance_id text,
  construct_type text,
  construct_name text,
  start_time timestamp,
  duration bigint,
  end_time timestamp,
  process_instance_id uuid,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id, experiment_id), construct_instance_id, process_instance_id)
);

-- Environment metadata
CREATE TABLE container_properties (
  container_properties_id uuid,
  container_id text,
  experiment_id text,
  trial_id text,
  host_id uuid,
  environment map<text, text>,
  image text,
  labels list<text>,
  links list<text>,
  log_driver text,
  u_limits map<text, int>,
  volume_driver text,
  volumes_from list<text>,
  cpu_shares int,
  cpu_set_cpus text,
  cpu_set_mems text,
  cpu_quota int,
  cpu_period int,
  blkio_weight int,
  mem_limit text,
  mem_swap_limit text,
  mem_reservation_limit text,
  mem_kernel_limit text,
  memory_swappiness int,
  oom_kill_disable boolean,
  privileged boolean,
  read_only boolean,
  restart text, 
  user text,
  name text,
  network text,
  restart_policy text,
  PRIMARY KEY ((trial_id, experiment_id), container_properties_id, host_id)
);

CREATE TABLE host_properties (
  host_id uuid,
  cpu_cfs_period boolean,
  cpu_cfs_quota boolean,
  debug boolean,
  discovery_backend text,
  docker_root_dir text,
  driver text,
  driver_status list<text>,
  execution_driver text,
  experimental_build boolean,
  http_proxy text,
  https_proxy text,
  ipv4_forwarding boolean,
  index_server_address text,
  init_path text,
  init_sha1 text,
  kernel_version text,
  labels list<text>,
  mem_total bigint,
  memory_limit boolean,
  n_cpu int,
  n_events_listener int,
  n_fd int,
  n_goroutines int,
  name text,
  no_proxy text,
  oom_kill_disable boolean,
  operating_system text,
  swap_limit boolean,
  system_time text,
  server_version text,
  docker_version text,
  docker_os text,
  docker_kernel_version text,
  docker_go_version text,
  docker_git_commit text,
  docker_arch text,
  docker_api_version text,
  docker_experimental boolean,
  alias text,
  external_ip text,
  internal_ip text,
  hostname text, 
  purpose text,
  PRIMARY KEY(host_id)
);

-- Experiment metrics
CREATE TABLE exp_process_duration (
  process_duration_weighted_avg double,
  process_duration_q1_min double,
  process_duration_q1_max double,
  process_duration_q2_min double,
  process_duration_q2_max double,
  process_duration_q3_min double,
  process_duration_q3_max double,
  process_duration_p95_min double,
  process_duration_p95_max double,
  process_duration_ci095_min double,
  process_duration_ci095_max double,
  process_duration_median_min double,
  process_duration_median_max double,
  process_duration_mode_min double,
  process_duration_mode_max double,
  process_duration_min double,
  process_duration_max double,
  process_duration_sd double,
  process_duration_best list<text>,
  process_duration_average list<text>,
  process_duration_worst list<text>,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_cpu (
  cpu_weighted_avg double,
  cpu_q1_min double,
  cpu_q1_max double,
  cpu_q2_min double,
  cpu_q2_max double,
  cpu_q3_min double,
  cpu_q3_max double,
  cpu_p95_min double,
  cpu_p95_max double,
  cpu_ci095_min double,
  cpu_ci095_max double,
  cpu_median_min double,
  cpu_median_max double,
  cpu_min double,
  cpu_max double,
  cpu_sd double,
  cpu_best list<text>,
  cpu_average list<text>,
  cpu_worst list<text>,
  experiment_id text,
  container_id text,
  PRIMARY KEY (experiment_id, container_id)
);

CREATE TABLE exp_cpu_core (
  cpu_weighted_avg list<double>,
  cpu_q1_min list<double>,
  cpu_q1_max list<double>,
  cpu_q2_min list<double>,
  cpu_q2_max list<double>,
  cpu_q3_min list<double>,
  cpu_q3_max list<double>,
  cpu_p95_min list<double>,
  cpu_p95_max list<double>,
  cpu_ci095_min list<double>,
  cpu_ci095_max list<double>,
  cpu_median_min list<double>,
  cpu_median_max list<double>,
  cpu_min list<double>,
  cpu_max list<double>,
  cpu_sd list<double>,
  experiment_id text,
  container_id text,
  PRIMARY KEY (experiment_id, container_id)
);

CREATE TABLE exp_ram (
  ram_weighted_avg double,
  ram_q1_min double,
  ram_q1_max double,
  ram_q2_min double,
  ram_q2_max double,
  ram_q3_min double,
  ram_q3_max double,
  ram_p95_min double,
  ram_p95_max double,
  ram_ci095_min double,
  ram_ci095_max double,
  ram_median_min double,
  ram_median_max double,
  ram_mode_min double,
  ram_mode_max double,
  ram_min double,
  ram_max double,
  ram_sd double,
  ram_best list<text>,
  ram_average list<text>,
  ram_worst list<text>,
  experiment_id text,
  container_id text,
  PRIMARY KEY (experiment_id, container_id)
);

CREATE TABLE exp_ram_integral (
  integral_avg double,
  integral_min double,
  integral_max double,
  integral_q1 double,
  integral_q2 double,
  integral_q3 double,
  integral_p95 double,
  integral_ci095_min double,
  integral_ci095_max double,
  integral_me double,
  integral_median double,
  integral_mode list<double>,
  integral_sd double,
  experiment_id text,
  PRIMARY KEY (experiment_id, container_id)
);

CREATE TABLE exp_cpu_integral (
  integral_avg double,
  integral_min double,
  integral_max double,
  integral_q1 double,
  integral_q2 double,
  integral_q3 double,
  integral_p95 double,
  integral_ci095_min double,
  integral_ci095_max double,
  integral_me double,
  integral_median double,
  integral_mode list<double>,
  integral_sd double,
  experiment_id text,
  PRIMARY KEY (experiment_id, container_id)
);

CREATE TABLE exp_throughput (
  throughput_avg double,
  throughput_min double,
  throughput_max double,
  throughput_q1 double,
  throughput_q2 double,
  throughput_q3 double,
  throughput_p95 double,
  throughput_ci095_min double,
  throughput_ci095_max double,
  throughput_me double,
  throughput_median double,
  throughput_mode list<double>,
  throughput_sd double,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_number_of_process_instances (
  number_of_process_instances_avg double,
  number_of_process_instances_min bigint,
  number_of_process_instances_max bigint,
  number_of_process_instances_q1 bigint,
  number_of_process_instances_q2 bigint,
  number_of_process_instances_q3 bigint,
  number_of_process_instances_p95 bigint,
  number_of_process_instances_ci095_min double,
  number_of_process_instances_ci095_max double,
  number_of_process_instances_me double,
  number_of_process_instances_median bigint,
  number_of_process_instances_mode list<bigint>,
  number_of_process_instances_sd double,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_byte_size (
  size_avg double,
  size_min bigint,
  size_max bigint,
  size_q1 bigint,
  size_q2 bigint,
  size_q3 bigint,
  size_p95 bigint,
  size_ci095_min double,
  size_ci095_max double,
  size_me double,
  size_median bigint,
  size_mode list<bigint>,
  size_sd double,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_io (
  reads_avg double,
  reads_min bigint,
  reads_max bigint,
  reads_q1 bigint,
  reads_q2 bigint,
  reads_q3 bigint,
  reads_p95 bigint,
  reads_ci095_min double,
  reads_ci095_max double,
  reads_me double,
  reads_median bigint,
  reads_mode list<bigint>,
  reads_sd double,
  writes_avg double,
  writes_min bigint,
  writes_max bigint,
  writes_q1 bigint,
  writes_q2 bigint,
  writes_q3 bigint,
  writes_p95 bigint,
  writes_ci095_min double,
  writes_ci095_max double,
  writes_me double,
  writes_median bigint,
  writes_mode list<bigint>,
  writes_sd double,
  total_avg double,
  total_min bigint,
  total_max bigint,
  total_q1 bigint,
  total_q2 bigint,
  total_q3 bigint,
  total_p95 bigint,
  total_ci095_min double,
  total_ci095_max double,
  total_me double,
  total_median bigint,
  total_mode list<bigint>,
  total_sd double,
  experiment_id text,
  container_id text,
  device text,
  PRIMARY KEY (experiment_id, container_id, device)
);

-- Trial metrics
CREATE TABLE trial_process_duration (
  process_duration_num_data_points double,
  process_duration_avg double,
  process_duration_q1 double,
  process_duration_q2 double,
  process_duration_q3 double,
  process_duration_p95 double,
  process_duration_mean double,
  process_duration_ci095_min double,
  process_duration_ci095_max double,
  process_duration_me double,
  process_duration_median double,
  process_duration_mode list<double>,
  process_duration_min double,
  process_duration_max double,
  process_duration_sd double,
  experiment_id text,
  trial_id text,
  PRIMARY KEY (experiment_id, trial_id)
);

CREATE TABLE trial_cpu (
  cpu_num_data_points bigint,
  cpu_avg double,
  cpu_q1 double,
  cpu_q2 double,
  cpu_q3 double,
  cpu_p95 double,
  cpu_mean double,
  cpu_ci095_min double,
  cpu_ci095_max double,
  cpu_me double,
  cpu_median double,
  cpu_min double,
  cpu_max double,
  cpu_sd double,
  cpu_integral double,
  experiment_id text,
  trial_id text,
  container_id text,
  PRIMARY KEY (experiment_id, container_id, trial_id)
);

CREATE TABLE trial_cpu_core (
  cpu_num_data_points bigint,
  cpu_avg list<double>,
  cpu_q1 list<double>,
  cpu_q2 list<double>,
  cpu_q3 list<double>,
  cpu_p95 list<double>,
  cpu_mean list<double>,
  cpu_ci095_min list<double>,
  cpu_ci095_max list<double>,
  cpu_me list<double>,
  cpu_median list<double>,
  cpu_min list<double>,
  cpu_max list<double>,
  cpu_sd list<double>,
  cpu_integral list<double>,
  experiment_id text,
  trial_id text,
  container_id text,
  PRIMARY KEY (experiment_id, container_id, trial_id)
);

CREATE TABLE trial_ram (
  ram_num_data_points bigint,
  ram_avg double,
  ram_q1 double,
  ram_q2 double,
  ram_q3 double,
  ram_p95 double,
  ram_mean double,
  ram_ci095_min double,
  ram_ci095_max double,
  ram_me double,
  ram_median double,
  ram_mode list<double>,
  ram_min double,
  ram_max double,
  ram_sd double,
  ram_integral double,
  experiment_id text,
  trial_id text,
  container_id text,
  PRIMARY KEY (experiment_id, container_id, trial_id)
);

CREATE TABLE trial_throughput (
  throughput double,
  execution_time bigint,
  experiment_id text,
  trial_id text,
  PRIMARY KEY (experiment_id, trial_id)
);

CREATE TABLE trial_number_of_process_instances (
  number_of_process_instances bigint,
  experiment_id text,
  trial_id text,
  PRIMARY KEY (experiment_id, trial_id)
);

CREATE TABLE trial_byte_size (
  size bigint,
  experiment_id text,
  trial_id text,
  PRIMARY KEY (experiment_id, trial_id)
);

CREATE TABLE trial_io (
  reads bigint,
  writes bigint,
  total bigint,
  experiment_id text,
  trial_id text,
  container_id text,
  device text,
  PRIMARY KEY (experiment_id, container_id, trial_id, device)
);