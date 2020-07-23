import spock.lang.Shared
import spock.lang.Specification

class ScriptVerificationSpec extends Specification{

    @Shared def groovyTestContainer = null
    @Shared def scriptsToRun = null

    def setupSpec() {
        groovyTestContainer = new GroovyTestContainer()
        assert groovyTestContainer!=null

        scriptsToRun = groovyTestContainer.loadScripts()
    }

    def "Loaded scripts are not null"() {
        when:
        def scriptsList = scriptsToRun
        then:
        scriptsList.size()!=-1
    }

    def "Scripts evaluation"() {
        when:
            scriptsToRun.size()!=-1
        then:
            for(File script: scriptsToRun) {
                assert groovyTestContainer.testScript(script) : ("Script test failed for : " + script.getName())
            }
    }

}
