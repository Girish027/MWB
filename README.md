## NL Tools Tagger

### UI

#### Git

[247 Git url](https://github.home.247-inc.net/)

[247 Tagger Git url](https://github.home.247-inc.net/advancedprototypes/ui)

```
git clone https://github.home.247-inc.net/advancedprototypes/ui
```
#### Installation.

Refer this for local setup  :[Installation steps](https://247inc.atlassian.net/wiki/spaces/AIT/pages/2015560522/MWB+Local+Setup)

It is optional to install Gradle but you do not need - once you are inside the project directory - you can type :
```
./gradlew build 
```
(to build with all test cases)
```
./gradlew build -x test 
```
(to build without test cases)

If you have any errors, please ensure that you have Java 8 installed,any server connecting to mysql db is running,elastic search is running.

#### Run the Java Server

```./gradlew bootRun``` ← once it hits 88% it means the server is running - its not hanging - open another terminal window to update the database

Once its running, open your browser to:-

https://localhost:8443 

If you see an advanced warning in Chrome, expand it and click accept 

#### Deleting the SQLite DB

At times, you may be asked to delete the SQLite database. Stop the project from running and then do:-

```rm nl_tools.db``` ← removes the file

Start the project: ```./gradlew bootRun```

```./filldb.sh``` ← populates the db - you can open this file in your IDE and replace the user credentials with your own

You may also need to clean the project

```./gradlew clean ```

then 

```./gradlew bootRun```

Or combine the two: ```./gradlew clean bootRun ```


#### Building with Webpack

```$ webpack -w ```

This will build and watch the files


#### Browser API calls

get all projects

https://tagging.247-inc.com:8443/nltools/v1/projects/ 

https://tagging.247-inc.com:8443/nltools/v1/projects/?pretty

fetch(`/nltools/v1/projects`)

get project by id 

https://tagging.247-inc.com:8443/nltools/v1/projects/8    

fetch(`/nltools/v1/projects` + projectId)    

# Vagrant

see https://247inc.atlassian.net/wiki/display/APT/How+to+Create+Vagrant+Environment   

### Auto Categorizer

you need to clone https://github.home.247-inc.net/advancedprototypes/auto-categorizer-web project using below to make sure it gets all of the submodules, clone into the same parent folder that contains your other git projects that you have mapped to Vagrant

```
git clone --recursive git@github.home.247-inc.net:advancedprototypes/auto-categorizer-web.git
```

Then you need to run command below so it builds the executing jar
```
./gradlew build
```

You need to make the following configuration changes in application.properties file

```
# Auto Categorizer #
tfs.auto-categorizer.categorize.enabled=true
tfs.auto-categorizer.categorize.url=http://localhost:9090/nltools/v1/categorize/
```


### Elastic Search 

open elasticsearch.properties and update:    
```
apiPort=9400    
transportPort=9310    
```
### Logstash 

Install Logstash 2.4.0 not the latest 5.0     
https://www.elastic.co/downloads/past-releases/logstash-2-4-0     

###Update this line with your own path within application.properties:     

## ElasticSearch 
```
tfs.elastic.host=localhost:9400
tfs.elastic.index-name=nltools
```
## Logstash # 
be careful about adding extra whitespace at the end!
 ```
tfs.logstash.exec-timeout=90000
tfs.logstash.check-exec-timeout=30000
tfs.logstash.exec=/Users/bflowers/logstash/logstash-2.4.0/bin/logstash
```
## Cluster   
http://localhost:9400/_plugin/kopf/#!/cluster     
everytime you transform that doc count will increase as we index more records from CSV each row in CSV amounts to 2 counts in that count

## Watching the Logs

`$ tail -F logs/nltools.log `

## cookie developing locally

If you happen to see this while developing locally, either quit chrome and launch it again... 

```
{"code":401,"errorCode":"401","message":"Invalid credentials"}
```

...or try to remove 247inc.oktapreview.com cookie


## Dev setup 
confluence link: https://247inc.atlassian.net/wiki/spaces/AIT/pages/169911648/NL+Workbench+developer+setup


## Proxy in developer set up in intellij can be achieved by adding the lines in NLtoolsUiApplication.java 

 local proxy is needed for deployment api testing with production deploy2 

    System.setProperty("http.proxyHost", "proxy-grp1.lb-priv.sv2.247-inc.net");
    System.setProperty("http.proxyPort", "3128");
    System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1");
    System.setProperty("https.proxyHost", "proxy-grp1.lb-priv.sv2.247-inc.net");
    System.setProperty("https.proxyPort", "3128");
 
 
## For Docker File

 Currently only slave 06 will work with docker set which has to hit psr ES.

 you might need to coordinate with CICD to be able to use any slave

 So do not change the agent or node in  Jenkins file till all other slave are in same networks as slave06

 Container name of app is hardcoded , so two jobs cannot run at same time . This can be looked into later by using branch name for ui container .

## Future enhancements will be to create custom images using : 
```
 GIT_BRANCH=`git rev-parse --abbrev-ref HEAD` 
 ```
should give current branch which it does not know and gives HEAD in docker in jenkins. So you have to look at getting branch and build number and use it for Jar creation.

##Refer this for the steps taken to upgrade Spring boot Upgrade 
https://247inc.atlassian.net/wiki/spaces/AIT/pages/643900660/Spring+boot+Upgrade+Steps

 SpringUpgrade_DockerWith_EsUpgrade has changes with ES upgrade and Latest Spring boot. BootRun works file . Junits would have to be looked at.

 tasks.withType(Test) needs to be changed to use dynamic proxies from docker file and set through script 

docker.jenkins.env
