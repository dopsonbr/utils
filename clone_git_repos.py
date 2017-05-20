#!/usr/bin/env python3

import requests
import os
import sys
from git import Repo, RemoteProgress

# Relative directory that the repos are to be cloned to
destination_directory = sys.argv[1]
os.chdir(destination_directory)
print('Cloning repos to %s' % os.getcwd())

# Jenkins2 Pipeline DSL Credential naming conventions
user = os.getenv('GIT_LOGIN_USR')
token = os.getenv('GIT_LOGIN_PSW')
# For easy comptability with Enterprise Github
url = os.getenv('GIT_URL')

if user is None:
    exit('User not supplied')
if token is None:
    exit('Token not supplied')
if url is None:
    url = 'https://api.github.com/'


class Progress(RemoteProgress):
    def update(self, op_code, cur_count, max_count=None, message=''):
        print(self._cur_line)


query = 'user/repos'
r = requests.get(url + query, auth=(user, token))
if r.status_code != 200:
    exit('Http error occured with status code %s' % r.status_code)

repos = r.json()
existing_repos = os.listdir()
for repo in repos:
    name = repo['name']
    print('Checking on repo %s' % name)
    if name not in existing_repos:
        print('%s doesnt not exist; cloning now' % name)
        Repo.clone_from(repo['clone_url'], name, progress=Progress())
    else:
        print('%s already exists; updating now' % name)
        # TODO check if repo is not dirty and update
        print('TODO check if repo is not dirty and update')
