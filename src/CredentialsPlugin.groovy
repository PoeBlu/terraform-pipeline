class CredentialsPlugin implements BuildStagePlugin {
    private static globalBuildCredentials = []
    private buildCredentials = []

    public static void init() {
        def plugin = new CredentialsPlugin()

        BuildStage.addPlugin(plugin)
    }

    public CredentialsPlugin() {
        this.buildCredentials = globalBuildCredentials.clone()
    }

    public static withBuildCredentials(Map options = [:], String credentialsId) {
        // Groovy is terrible
        Map optionsWithDefaults = populateDefaults(options, credentialsId)
        globalBuildCredentials << optionsWithDefaults
        return this
    }

    @Override
    public void apply(BuildStage buildStage) {
        buildStage.decorate(addBuildCredentials())
    }

    private addBuildCredentials() {
        def credentials = this.buildCredentials
        return { closure ->
            def results = credentials.collect { credential ->
                usernamePassword(credential)
            }

            withCredentials(results) {
                closure()
            }
        }
    }

    public static Map populateDefaults(Map options = [:], String credentialsId)  {
        def credentialsOptions = options.clone()
        credentialsOptions['credentialsId'] = credentialsId
        credentialsOptions['usernameVariable'] = credentialsOptions['usernameVariable'] ?: "${toEnvironmentVariable(credentialsId)}_USERNAME".toString()
        credentialsOptions['passwordVariable'] = credentialsOptions['passwordVariable'] ?: "${toEnvironmentVariable(credentialsId)}_PASSWORD".toString()

        return credentialsOptions
    }

    public static String toEnvironmentVariable(String value) {
        value.toUpperCase().replaceAll('-', '_')
    }

    public static getBuildCredentials() {
        return globalBuildCredentials
    }

    public static void reset() {
        globalBuildCredentials = []
    }

}
