// Uniq name for the pod or slave 
def k8slabel = "jenkins-pipeline-${UUID.randomUUID().toString()}"

properties([
    [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false], 
    parameters([
        booleanParam(defaultValue: false, description: 'Please select to apply all changes to the environment', name: 'applyChanges'), 
        booleanParam(defaultValue: false, description: 'Please select to destroy all changes to the environment', name: 'destroyChanges'), 
        string(defaultValue: '', description: 'Please provide the docker to deploy ', name: 'selectedDockerImage', trim: true), 
        choice(choices: ['dev', 'qa', 'stage', 'prod'], description: 'Please provide the environment to deploy ', name: 'environment')
        ])
        ])



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
        - name: fuchicorptools
          image: fuchicorp/buildtools
          imagePullPolicy: Always
          command:
          - cat
          tty: true
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
            git 'https://github.com/AigerimMaden/jenkins-class.git'
        }


        stage("Apply Plan") {
            container("fuchicorptools") {
                if (!params.destroyChanges) {
                    if (params.applyChanges) {
                        println("Applying the changes!")
                    } else {
                        println("Planing the changes")
                    }
                }





                sh 'kubectl version'
            }
        }

        stage("destroy") {
            if (!applyChanges) {
                if (destroyChanges) {
                    println("Destroying everything")
                } 
            } else {
                println("Sorry I can not destroy and apply!")
            }
        }
      }
    }