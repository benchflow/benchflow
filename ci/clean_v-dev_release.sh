#!/bin/sh
# Clean the v-dev release and tag we are using to release development version in CI
# Meants to: be used in Travis-CI
# Accepts Envs:
# - USER_NAME: the name of the user
#	- REPO_NAME: the name of the repository under USER_NAME user, without leading /
#	- GITHUB_ACCESS_TOKEN: the access token used to interact with git (generally stored in Travis)
set -e
# install before_deploy dependencies (JSON parser)
sudo wget https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 -O /usr/bin/jq
sudo chmod +x /usr/bin/jq
# configure the Travis user for Git
git config --global user.email "builds@travis-ci.com"
git config --global user.name "Travis CI"
# configure tag (prepended v- in order to ignore the build on push)
export GIT_TAG=v-dev
# delete the release, as well as its assets, if it exists
export id=$(curl -sS -u x-oauth-basic:$GITHUB_ACCESS_TOKEN -X GET https://api.github.com/repos/$USER_NAME/$REPO_NAME/releases/tags/$GIT_TAG | /usr/bin/jq '.id')
if [ "$id" != "null" ]; then curl -u x-oauth-basic:$GITHUB_ACCESS_TOKEN -X DELETE https://api.github.com/repos/$USER_NAME/$REPO_NAME/releases/$id; fi
# delete tag if it already exists
git tag | if [ $(grep -c $GIT_TAG) -eq 1 ]; then git tag -d $GIT_TAG; fi
git push -q https://$GITHUB_ACCESS_TOKEN:x-oauth-basic@github.com/$USER_NAME/$REPO_NAME :refs/tags/$GIT_TAG
# push the new tag
git tag $GIT_TAG -a -m "Development Tag"
git push -q https://$GITHUB_ACCESS_TOKEN:x-oauth-basic@github.com/$USER_NAME/$REPO_NAME --tags
