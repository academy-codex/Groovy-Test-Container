import java.io.File;
import java.util.List;

public interface TestContainer {

    List<File> loadScripts() throws TestCotainerBuildException;
    boolean testScript(File scriptFile) throws TestCotainerBuildException;
}
