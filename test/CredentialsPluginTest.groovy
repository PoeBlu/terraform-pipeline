import static org.junit.Assert.*

import org.junit.*
import org.junit.runner.RunWith
import de.bechte.junit.runners.context.HierarchicalContextRunner
import static org.hamcrest.Matchers.*

@RunWith(HierarchicalContextRunner.class)
class CredentialsPluginTest {
    public class Init {
        @After
        void resetPlugins() {
            BuildStage.resetPlugins()
            CredentialsPlugin.reset()
        }

        @Test
        void modifiesBuildStage() {
            CredentialsPlugin.init()

            Collection actualPlugins = BuildStage.getPlugins()

            assertThat(actualPlugins, hasItem(instanceOf(CredentialsPlugin.class)))
        }
    }

    public class WithBuildCredentials {
        @After
        void resetPlugin() {
            CredentialsPlugin.reset()
        }

        @Test
        void addsCredentialsForBuildStage() {
            CredentialsPlugin.withBuildCredentials("credentials1")

            def buildCredentials = CredentialsPlugin.getBuildCredentials()
            assertThat(buildCredentials, hasSize(1))

            def credential = buildCredentials.find { it['credentialsId'] == "credentials1" }
            assertThat(credential, notNullValue())
        }

        @Test
        void addsMultipleCredentialsForBuildStage() {
            CredentialsPlugin.withBuildCredentials("credentials1")
            CredentialsPlugin.withBuildCredentials("credentials2")

            def buildCredentials = CredentialsPlugin.getBuildCredentials()
            assertThat(buildCredentials, hasSize(2))

            def credential1 = buildCredentials.find { it['credentialsId'] == "credentials1" }
            assertThat(credential1, notNullValue())
            def credential2 = buildCredentials.find { it['credentialsId'] == "credentials2" }
            assertThat(credential2, notNullValue())
        }
    }

    public class ToEnvironmentVariable {
        @Test
        void convertsLowercaseToUppercase() {
            String lower = "mYvar"

            String result = CredentialsPlugin.toEnvironmentVariable(lower)

            assertThat(result, is(equalTo("MYVAR")))
        }

        @Test
        void convertsDashesToUnderscore() {
            String withDash = "MY-VAR"

            String result = CredentialsPlugin.toEnvironmentVariable(withDash)

            assertThat(result, is(equalTo("MY_VAR")))
        }

        @Test
        void convertsAllTheThings() {
            String withAllTheThings = "my-Var"

            String result = CredentialsPlugin.toEnvironmentVariable(withAllTheThings)

            assertThat(result, is(equalTo("MY_VAR")))
        }
    }

    public class PopulateDefaults {
        @Test
        void populatesCredentialsId() {
            String credentialsId = 'myId'

            Map results = CredentialsPlugin.populateDefaults(credentialsId)

            assertThat(results['credentialsId'], is(equalTo(credentialsId)))
        }

        @Test
        void defaultsUserVariableUsingCredentialsId() {
            String credentialsId = 'myId'

            Map results = CredentialsPlugin.populateDefaults(credentialsId)

            assertThat(results['usernameVariable'], is(equalTo("MYID_USERNAME")))
        }

        @Test
        void defaultsPasswordVariableUsingCredentialsId() {
            String credentialsId = 'myId'

            Map results = CredentialsPlugin.populateDefaults(credentialsId)

            assertThat(results['passwordVariable'], is(equalTo("MYID_PASSWORD")))
        }

        @Test
        void allowsCustomUserVariable() {
            String credentialsId = 'myId'
            String customUserVariable = "MY_CUSTOM_USERNAME_VARIABLE"

            Map results = CredentialsPlugin.populateDefaults(credentialsId, usernameVariable: customUserVariable)

            assertThat(results['usernameVariable'], is(equalTo(customUserVariable)))
        }

        @Test
        void allowsCustomPasswordVariable() {
            String credentialsId = 'myId'
            String customPasswordVariable = "MY_CUSTOM_PASSWORD_VARIABLE"

            Map results = CredentialsPlugin.populateDefaults(credentialsId, passwordVariable: customPasswordVariable)

            assertThat(results['passwordVariable'], is(equalTo(customPasswordVariable)))
        }
    }
}

