properties([
    [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false], 
    parameters([
        booleanParam(defaultValue: false, description: 'Please select to apply all changes to the environment', name: 'applyChanges'), 
        booleanParam(defaultValue: false, description: 'Please select to destroy all changes to the environment', name: 'destroyChanges'), 
        string(defaultValue: '', description: 'Please provide the docker to deploy ', name: 'selectedDockerImage', trim: true), 
        choice(choices: ['dev', 'qa', 'stage', 'prod'], description: 'Please provide the environment to deploy ', name: 'environment')
        ])
        ])
println(
    """
    Apply changes: ${params.applyChanges}
    Destroy changes: ${params.destroyChanges}
    Docker  image:  ${params.selectedDockerImage}
    Environment: ${params.environment}
    """
)