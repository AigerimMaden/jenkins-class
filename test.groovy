// Uniq name for the pod or slave 
def k8slabel = "jenkins-pipeline-${UUID.randomUUID().toString()}"
def branch = "${scm.branches[0].name}".replaceAll(/^\*\//, '')

properties([
  [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false], 
  parameters([
    booleanParam(defaultValue: false, description: 'Select to be able to push to latest', name: 'pushLatest')
    ])
  ])
// yaml def for slaves 
def slavePodTemplate = """
      metadata:
        labels:
          k8s-label: ${k8slabel}
        annotations:
          jenkinsjoblabel: ${env.JOB_NAME}-${env.BUILD_NUMBER}
      spec:
        affinity:
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                - key: component
                  operator: In
                  values:
                  - jenkins-jenkins-master
              topologyKey: "kubernetes.io/hostname"
        containers:
        - name: docker
          image: docker:latest
          imagePullPolicy: IfNotPresent
          command:
          - cat
          tty: true
          volumeMounts:
            - mountPath: /var/run/docker.sock
              name: docker-sock
        serviceAccountName: default
        securityContext:
          runAsUser: 0
          fsGroup: 0
        volumes:
          - name: docker-sock
            hostPath:
              path: /var/run/docker.sock
    """
    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {

        stage("Checkout SCM") {
            checkout scm 
        }
        dir('deployments/docker') {
            container('docker') {
                withCredentials([usernamePassword(credentialsId: 'docker-creds', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                    stage('Docker Build') {
                        sh 'docker build -t artemis .'
                    }
                    stage('Docker Login') {
                        sh "docker login --username  $USERNAME  --password $PASSWORD " 
                    }

                    stage('Docker Push') {
                        if (params.pushLatest) {
                            println('Pushing the image to latest version!!')
                            sh "docker tag artemis aigerimmadenova/artemis:latest"
                            sh "docker push aigerimmadenova/artemis:latest"
                        } else {
                            println("Pushing the ${branch} version.")
                        }
                        sh "docker tag artemis aigerimmadenova/artemis:${branch}"
                        sh "docker push aigerimmadenova/artemis:${branch}"
                     }
                }
            }
            stage('checking') {
                sh 'ls -l'
            }
        }
      }
    }