@Library(['ciHelper@master']) _

def buildArgs1 = [:]
def buildArgs2 = [:]

def imageTag() {
//     gitCommitHash = sh (script: "git log -n 1 --pretty=format:'%h'", returnStdout: true).trim()
//     // TODO: please put your cec-id as the image tags. NOTE: no space in cec-id please.
//     gitCommitUser = "bange"
//     tag = gitCommitUser + "_${BUILD_NUMBER}_" + gitCommitHash
//     appVersion = sh (script: "grep -m1 'version' WBXmatsrainierSAPservice/advanced-diagnostic-meeting/pom.xml | cut -d'<' -f2 | cut -d'>' -f2", returnStdout: true).trim()
    appVersion = "1.3.0"
    tag = "${appVersion}" + "-" + "${BUILD_NUMBER}"
    return tag
}

def csr_metaBody = {
    artifact_id = imageTag() + "_csr"
    image_tag = imageTag()
    describe = "datahub csr ci pipeline"
}

pipeline {
    agent { label 'SJC' }
    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven "Maven3.6.3"
//         jdk "jdk1.8"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Jar File') {
            steps {
//                 script {
//                     sh "sed -i 's/: $SERVICE_ID/: $SERVICE_ID$HELM_SUFFIX/g' $SERVICE_ID/src/main/resources/bootstrap.yaml"
//                 }
//                 echo "Build by maven."
//                 sh '. /etc/profile'
//                 sh 'which mvn'
//                 sh 'which java'
//                 sh 'mvn --version'
                sh 'pwd'
                sh 'ls -la'
//                 sh 'cd WBXmatsrainierSAPservice'
//                 sh 'pwd'
//                 sh 'cd WBXmatsrainierSAPservice && mvn clean'
//                 sh 'rm -rf WBXmatsrainierSAPservice/advanced-diagnostic-meeting/lib/* || echo "delete lib error"'
//                 sh 'cp WBXmatsrainierSAPservice/advanced-diagnostic-meeting/settings.xml ~/.m2/'
//                 sh 'cp WBXmatsrainierSAPservice/advanced-diagnostic-meeting/settings-security.xml ~/.m2/'
//                 sh 'ls -la'
//                 sh 'cd WBXmatsrainierSAPservice && mvn install -pl advanced-diagnostic-common -Dmaven.test.skip=true'
//                 sh 'cd WBXmatsrainierSAPservice && mvn install -pl advanced-diagnostic-data -Dmaven.test.skip=true'
//                 sh 'cd WBXmatsrainierSAPservice/advanced-diagnostic-meeting && mvn package -Dmaven.test.skip=true'
//                 sh 'cd WBXmatsrainierSAPservice/advanced-diagnostic-meeting && ls -al lib'
            }
        }

        stage('Build Image') {
            steps {
                script {
                    buildArgs1 = [component: "csr", tag: imageTag(), metadata: csr_metaBody]
                }
                script {
                    buildCI(this, buildArgs1)
                }
            }
        }
   }
}
