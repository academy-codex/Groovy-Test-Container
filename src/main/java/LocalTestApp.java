public class LocalTestApp {
    public static void main(String[] args) throws TestCotainerBuildException {

        GroovyTestContainer groovyTestContainer = new GroovyTestContainer();
        groovyTestContainer.loadScripts();
    }
}
