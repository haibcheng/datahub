@Library(['ciHelper@master']) _

def buildArgsCsr = [:]
def buildArgsGms = [:]
def buildArgsActions = [:]
def buildArgsFrontend = [:]

def csr_imageTag() {
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
    artifact_id = csr_imageTag() + "_csr"
    image_tag = csr_imageTag()
    describe = "datahub csr ci pipeline"
}

def gms_imageTag() {
    appVersion = "1.3.0"
    tag = "${appVersion}" + "-" + "${BUILD_NUMBER}"
    return tag
}

def gms_metaBody = {
    artifact_id = gms_imageTag() + "_gms"
    image_tag = gms_imageTag()
    describe = "datahub gms ci pipeline"
}

def actions_imageTag() {
    appVersion = "1.3.0"
    tag = "${appVersion}" + "-" + "${BUILD_NUMBER}"
    return tag
}

def actions_metaBody = {
    artifact_id = actions_imageTag() + "_actions"
    image_tag = actions_imageTag()
    describe = "datahub actions ci pipeline"
}

def frontend_imageTag() {
    appVersion = "1.3.0"
    tag = "${appVersion}" + "-" + "${BUILD_NUMBER}"
    return tag
}

def frontend_metaBody = {
    artifact_id = frontend_imageTag() + "_frontend"
    image_tag = frontend_imageTag()
    describe = "datahub frontend ci pipeline"
}

pipeline {
    agent { label 'SJC' }
    parameters {
        choice(name: 'DATAHUB_SERVICE', choices: ['csr', 'gms', 'actions', 'frontend'], description: 'Pick datahub service')
    }
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
                    buildArgsCsr = [component: "csr", tag: csr_imageTag(), metadata: csr_metaBody]
                    buildArgsGms = [component: "gms", tag: gms_imageTag(), metadata: gms_metaBody]
                    buildArgsActions = [component: "actions", tag: actions_imageTag(), metadata: actions_metaBody]
                    buildArgsFrontend = [component: "frontend", tag: frontend_imageTag(), metadata: frontend_metaBody]
                }
                script {
                    echo "Build CI for ${params.DATAHUB_SERVICE}"
                    sh """#!/bin/bash -xe
                          echo \"Build CI for ${params.DATAHUB_SERVICE}\"
                    """
                    if (params.DATAHUB_SERVICE == 'csr') {
                        buildCI(this, buildArgsCsr)

                        imageTag = "csr:" + csr_imageTag()
                        sh """#!/bin/bash -xe
                              echo \"Removing ${imageTag}\"
                              docker image rm -f \"${imageTag}\"
                              echo \"Remove local ${imageTag} successfully\"
                        """
                    } else if (params.DATAHUB_SERVICE == 'gms') {
                        buildCI(this, buildArgsGms)

                        imageTag = "gms:" + gms_imageTag()
                        sh """#!/bin/bash -xe
                              echo \"Removing ${imageTag}\"
                              docker image rm -f \"${imageTag}\"
                              echo \"Remove local ${imageTag} successfully\"
                        """
                    } else if (params.DATAHUB_SERVICE == 'actions') {
                        buildCI(this, buildArgsActions)

                        imageTag = "actions:" + actions_imageTag()
                        sh """#!/bin/bash -xe
                              echo \"Removing ${imageTag}\"
                              docker image rm -f \"${imageTag}\"
                              echo \"Remove local ${imageTag} successfully\"
                        """
                    } else if (params.DATAHUB_SERVICE == 'frontend') {
                        buildCI(this, buildArgsFrontend)

                        imageTag = "frontend:" + frontend_imageTag()
                        sh """#!/bin/bash -xe
                              echo \"Removing ${imageTag}\"
                              docker image rm -f \"${imageTag}\"
                              echo \"Remove local ${imageTag} successfully\"
                        """
                    }

                    sh """#!/bin/bash -xe
                          docker images -a | grep \"${params.DATAHUB_SERVICE}\"
                    """
                }
            }
        }
   }
}
