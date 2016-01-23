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
CREATE TABLE environment_data (
  environment_data_id uuid,
  container_properties_id uuid,
  read_time text,
  cpu_percpu_usage list<bigint>,
  cpu_total_usage bigint,
  cpu_percent_usage float,
  cpu_throttling_data map<text, bigint>,
  memory_usage bigint,
  memory_max_usage bigint,
  network_interfaces map<text, uuid>,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id, experiment_id), environment_data_id, container_properties_id)
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
  process_duration_avg float,
  process_duration_q1 float,
  process_duration_q2 float,
  process_duration_q3 float,
  process_duration_mean float,
  process_duration_ci095 float,
  process_duration_median float,
  process_duration_mode float,
  process_duration_min float,
  process_duration_max float,
  process_duration_sd float,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_cpu (
  cpu_avg float,
  cpu_q1 float,
  cpu_q2 float,
  cpu_q3 float,
  cpu_mean float,
  cpu_ci095 float,
  cpu_median float,
  cpu_mode float,
  cpu_min float,
  cpu_max float,
  cpu_sd float,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_ram (
  ram_avg float,
  ram_q1 float,
  ram_q2 float,
  ram_q3 float,
  ram_mean float,
  ram_ci095 float,
  ram_median float,
  ram_mode float,
  ram_min float,
  ram_max float,
  ram_sd float,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_throughput (
  throughput float,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

CREATE TABLE exp_number_of_process_instances (
  number_of_process_instances bigint,
  experiment_id text,
  PRIMARY KEY ((experiment_id))
);

-- Trial metrics
CREATE TABLE trial_process_duration (
  process_duration_avg float,
  process_duration_q1 float,
  process_duration_q2 float,
  process_duration_q3 float,
  process_duration_mean float,
  process_duration_ci095 float,
  process_duration_median float,
  process_duration_mode float,
  process_duration_min float,
  process_duration_max float,
  process_duration_sd float,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id,experiment_id))
);

CREATE TABLE trial_cpu (
  cpu_avg float,
  cpu_q1 float,
  cpu_q2 float,
  cpu_q3 float,
  cpu_mean float,
  cpu_ci095 float,
  cpu_median float,
  cpu_mode float,
  cpu_min float,
  cpu_max float,
  cpu_sd float,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id,experiment_id))
);

CREATE TABLE trial_ram (
  ram_avg float,
  ram_q1 float,
  ram_q2 float,
  ram_q3 float,
  ram_mean float,
  ram_ci095 float,
  ram_median float,
  ram_mode float,
  ram_min float,
  ram_max float,
  ram_sd float,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id,experiment_id))
);

CREATE TABLE trial_throughput (
  throughput float,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id,experiment_id))
);

CREATE TABLE trial_number_of_process_instances (
  number_of_process_instances bigint,
  experiment_id text,
  trial_id text,
  PRIMARY KEY ((trial_id,experiment_id))
);