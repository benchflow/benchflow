from setuptools import setup

setup(
        name='BenchFlow',
        version='0.1.0',
        py_modules=['benchflow'],
        install_requires=[
            'Click',
            'requests',
            'pygments'
        ],
        entry_points='''
            [console_scripts]
            benchflow=benchflow:client
        ''',
)
