#!/usr/bin/env bash

set -x

# for e in `ls /opt/rh/*/enable | grep -v nodejs8`; do source $e; done
source /opt/rh/devtoolset-8/enable
source /opt/rh/rh-git218/enable
source /opt/rh/rh-nodejs10/enable

eval `ssh-agent -s`
ssh-add /root/.ssh/id_rsa

# Set proxy url using proxy host's IP address.
# This avoids problems during npm install.
#
PROXY_IP=`node -e "
  const dns = require('dns');
  dns.resolve('$PROXY_HOST', (err, adrs) => {
    console.log(adrs ? adrs[0] : 'ERROR:' + (err ? err.message : 'unknown'))
  })"`

export http_proxy=http://$PROXY_HOST:$PROXY_PORT
export https_proxy=$http_proxy
export HTTP_PROXY=$http_proxy
export HTTPS_PROXY=$http_proxy

echo $http_proxy
set

export NO_COLOR=1
export DK_OUTPUT=/dk_output

PROJECT_NAME=nl-tools-ui-wrapper


GRP_ID=nltools
GRP_ID2=nlworkbench
ART_ID=$PROJECT_NAME

echo "### Check that git branch matches package version, i.e. develop -> SNAPSHOT, master -> != SNAPSHOT"
#GIT_BRANCH=`git branch` #  Assumes that we've checked out a named branch.
#GIT_BRANCH=`git rev-parse--abbrev-ref HEAD` #  Assumes that we've checked out a named branch.
HEAD_BRANCH=`git rev-parse --abbrev-ref HEAD`
GIT_BRANCH=${BRANCH_NAME:-$HEAD_BRANCH}
[[ -z "$GIT_BRANCH" ]] && echo "ERROR: cannot determine git branch" && exit 1

echo "BRANCH_NAME - $BRANCH_NAME"
echo "HEAD_BRANCH - $HEAD_BRANCH"
echo "GIT_BRANCH - $GIT_BRANCH"

#branch_name=$(git branch)

#GIT_BRANCH=$branch_name||GIT_BRANCH="Random"
[[ -z "$GIT_BRANCH" ]] && echo "ERROR: cannot determine git branch" && exit 1

PROJECT_VERSION="${ART_ID}-${GIT_BRANCH}"
PROJECT_VERSION_JAR="${ART_ID}-${GIT_BRANCH}-${BUILD_ID}"

echo "#############"
echo $PROJECT_VERSION
echo "#############"


NEXUS_REPO_TYPE=SANITY
PR_BRANCH_RX='^(PR-.*|HEAD)$'
[[ $GIT_BRANCH =~ $PR_BRANCH_RX ]]

echo "### BUILD project version $PROJECT_VERSION on $GIT_BRANCH branch"
npm config set proxy $http_proxy
npm config set https-proxy $https_proxy

echo "### Set up Gradle options"
export GRADLE_OPTS="-Dhttp.proxyHost=$PROXY_HOST -Dhttp.proxyPort=$PROXY_PORT"\
" -Dhttps.proxyHost=$PROXY_HOST -Dhttps.proxyPort=$PROXY_PORT"\
" -Dhttp.nonProxyHosts=*.tellme.com|127.0.0.1|*.247-inc.net"\
" -Dhttps.nonProxyHosts=*.tellme.com|127.0.0.1|*.247-inc.net"\
" -Dmaven.wagon.http.ssl.allowall=true"\
" -Dmaven.wagon.http.ssl.ignore.validity.dates=true"\
" -Dmaven.wagon.http.ssl.insecure=true"\
" -Dorg.gradle.daemon=false"

# gradlew migrateTestDB
./gradlew migrateTestDB --stacktrace; BUILD_EXIT_CODE=$?
[[ $BUILD_EXIT_CODE != 0 ]] && echo "gradlew migrateTestDB exit code: $BUILD_EXIT_CODE" && exit $BUILD_EXIT_CODE

#TODO not to be merged.
# gradlew test npmTest npmEslint --continue
./gradlew test npmTest npmEslint --continue; BUILD_EXIT_CODE=$?
[[ $BUILD_EXIT_CODE != 0 ]] && echo "gradlew test exit code: $BUILD_EXIT_CODE" && exit $BUILD_EXIT_CODE

# SonarQube
#./gradlew --info sonarqube -Dsonar.projectVersion=1.0.0; BUILD_EXIT_CODE=$?
#[[ $BUILD_EXIT_CODE != 0 ]] && echo "gradlew SonarQube exit code: $BUILD_EXIT_CODE" && exit $BUILD_EXIT_CODE

# gradlew clean
./gradlew clean; BUILD_EXIT_CODE=$?
[[ $BUILD_EXIT_CODE != 0 ]] && echo "gradlew clean exit code: $BUILD_EXIT_CODE" && exit $BUILD_EXIT_CODE

# gradlew bootWrapper
./gradlew bootWrapper; BUILD_EXIT_CODE=$?
[[ $BUILD_EXIT_CODE != 0 ]] && echo "gradlew bootWrapper exit code: $BUILD_EXIT_CODE" && exit $BUILD_EXIT_CODE

echo "### Set NEXUS URL"
NEXUS_REPO_URL=http://$NEXUS_HOST/nexus/content/repositories/sanity/$GRP_ID/$GRP_ID2/$PROJECT_VERSION/$PROJECT_VERSION_JAR.zip

NEXUS_REPO_ID_SANITY=nltools

if [[ $NEXUS_REPO_TYPE == "NONE" ]]; then
  echo "$GIT_BRANCH compiled successfully, but will not be deployed." && exit 0
fi

### DEPLOY ZIP ARCHIVE TO NEXUS

# mvn to nexus
echo "### Deploy artifact $ART_ID to $NEXUS_REPO_URL URL"
#http://nexus.cicd.sv2.247-inc.net/nexus/content/repositories/sanity/nltools/nlworkbench/nl-tools-ui-wrapper/1.0.0-develop-34/nl-tools-ui-wrapper-1.0.0-develop-34.zip
WS_LOCATION=$(pwd)
curl -u deployment:deployment123 --upload-file ./build/libs/nl-tools-ui-wrapper.zip $NEXUS_REPO_URL
#mvn -B deploy:deploy-file -s $WS_LOCATION/docker.maven.settings.xml -Durl=$NEXUS_REPO_URL -DrepositoryId=$NEXUS_REPO_ID_SANITY -DgroupId=$GRP_ID -DartifactId=$ART_ID -Dversion=$PROJECT_VERSION -Dpackaging=zip -Dfile=$WS_LOCATION/build/libs/nl-tools-ui-wrapper.zip -DgeneratePom=true -e
#echo "mvn -B deploy:deploy-file -s $(WS_LOCATION)/docker.maven.settings.xml -Durl=$NEXUS_REPO_URL_SANITY -DrepositoryId=$NEXUS_REPO_ID_SANITY -DgroupId=$GRP_ID -DartifactId=$ART_ID -Dversion=promoted -Dpackaging=zip -Dfile=/build/libs/nl-tools-ui-wrapper.zip -DgeneratePom=true -e"

# jacoco classPattern: '**/build/classes', execPattern: '**build/jacoco/test.exec'

exit 0
