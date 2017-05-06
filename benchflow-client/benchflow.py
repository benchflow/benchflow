import click
import requests
import subprocess
import os
import pygments
import zipfile
from pathlib import Path

exp_manager_address = os.getenv('EXPERIMENTS_MANAGER_ADDRESS')
cassandra_ip = os.getenv('CASSANDRA_IP')
cassandra_port = os.getenv('CASSANDRA_PORT')
drivers_maker_address = os.getenv('DRIVERS_MAKER_ADDRESS')

class V2(object):
    def __init__(self):
        self.session = requests.Session()
        self.session.headers = {
            'Accept': 'application/vnd.experiments-manager.v2+json'
        }

    def deploy(self, benchmark):
        filename = click.format_filename(benchmark)
        benchmark = {'benchmark': open(filename, 'rb')}
        click.echo('Deploying benchmark...')
        r = self.session.post('{}/deploy'.format(exp_manager_address), files=benchmark)
        if r.status_code == 200:
            benchmark_name = click.style(r.json()['deploy'], fg='red')
            click.echo('Benchmark {} successfully deployed.'.format(benchmark_name))
        else:
            click.echo(r.json())

    def run(self, benchmark_name):
        address = '{}/run/{}'.format(exp_manager_address, benchmark_name)
        r = self.session.post(address)
        if r.status_code == 200:
            experiment_id = click.style(r.json()['experimentId'], fg='red')
            trials = click.style(str(r.json()['trials']), fg='red')
            click.echo('Running experiment {} ({} trials).'.format(experiment_id, trials))
        else:
            click.echo(r.json())

    def status(self, run_id):
        raise click.ClickException('/status is not implemented for this api version yet.')


class Config(object):
    def __init__(self):
        self.version = 2
        self.api = V2()


pass_config = click.make_pass_decorator(Config, ensure=True)


@click.group()
@click.option('--version', default=2, help='api version')
def cli():
    pass


def zipdir(path):
    """Utility function to zip a directory"""
    p = Path(path)
    parent = (p / '..').resolve()
    archive_path = '{}/{}.zip'.format(parent, p.name)
    with zipfile.ZipFile(archive_path, 'w') as archive:
        for root, dirs, files in os.walk(path):
            for file in files:
                archive.write(os.path.join(root, file),
                              os.path.relpath(os.path.join(root, file), os.path.join(path, '..')))
    return archive_path


@cli.command()
@click.argument('benchmark_dir', type=click.Path(exists=True, file_okay=False),
                metavar='<benchmark_dir>')
@pass_config
def build(config, benchmark_dir):
    """Builds a BenchFlow benchmark"""
    if config.version == 'v2':
        p = Path(benchmark_dir)
        dd = (p / 'docker-compose.yml').resolve()
        bb = (p / 'benchflow-test.yml').resolve()
        models = (p / 'models').resolve()
        # sources = (p / 'sources').resolve()
        # sources_zip_path = zipdir(str(sources))
        benchmark_zip_path = '{}/{}.zip'.format(benchmark_dir, p.name)
        with zipfile.ZipFile(benchmark_zip_path, 'w') as archive:
            archive.write(str(dd), dd.name)
            archive.write(str(bb), bb.name)
            archive.write(str(models), models.name)
            for model in os.listdir(str(models)):
                if not model.startswith('.'): #ignore .DS_Store
                    archive.write('{}/{}'.format(str(models), model), 'models/' + model)
            # archive.write(sources_zip_path, 'sources.zip')
        # os.remove(sources_zip_path)
        benchmark_name = click.style(p.name, fg='red')
        click.echo('Benchmark {} successfully built.'.format(benchmark_name))
    else:
        cmd = ['make', 'build_for_benchflow']
        subprocess.call(cmd)


@cli.command()
def update():
    """Updates BenchFlow by pulling the latest image"""
    pass


@click.group()
def api():
    pass


@api.command()
@click.argument('benchmark', type=click.Path(exists=True, dir_okay=False),
                metavar='<benchmark>')
@pass_config
def deploy(config, benchmark):
    """Deploys a <benchmark>"""
    config.api.deploy(benchmark)


@api.command()
@click.argument('benchmark_name', metavar='<benchmark_name>')
@pass_config
def run(config, benchmark_name):
    """Runs a benchmark"""
    config.api.run(benchmark_name)


@api.command()
@click.argument('run_id', metavar='<run_id>')
@pass_config
def status(config, run_id):
    """Returns the status of an experiment run"""
    config.api.status(run_id)


@cli.command()
def cql():
    """Starts cqlsh"""
    cmd = ['cqlsh ' + cassandra_ip + ' ' + cassandra_port]
    subprocess.call(cmd, shell=True)


@cli.group()
def debug():
    """Debug commands, for development purposes"""
    pass


@debug.command()
@click.argument('configuration', type=click.Path(exists=True, dir_okay=False),
                metavar='<configuration>')
def convert(configuration):
    """Generates a Faban configuration file"""
    filename = click.format_filename(configuration)
    bfconfiguration = {'benchflow-benchmark': open(filename, 'rb')}
    click.echo('Address: ' + drivers_maker_address + '/convert')
    r = requests.post(drivers_maker_address + '/convert', files=bfconfiguration)
    click.echo(r.text)

@debug.command()
@click.argument('benchmark_name')
@click.argument('experiment_number')
@click.argument('total_trials')
def generate(benchmark_name, experiment_number, total_trials):
    """Generates a Faban driver"""
    driver_info = {
        'benchmarkName': benchmark_name,
        'experimentNumber': experiment_number,
        'trials': total_trials
    }
    click.echo('Generating driver for experiment {}.{} (trials: {})'.format(benchmark_name, experiment_number, total_trials))
    r = requests.post(drivers_maker_address + '/generatedriver', json=driver_info)
    click.echo(r)


@click.command(cls=click.CommandCollection, sources=[api, cli])
@click.option('--api-version', default='v2', type=click.Choice(['v1', 'v2']), help='Api version')
@pass_config
def client(config, api_version):
    config.version = api_version
    config.api = V1() if config.version == 'v1' else V2()