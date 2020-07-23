import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import java.io.*;
import java.net.URL;
import java.util.*;

import static constants.Constants.*;

public class GroovyTestContainer implements TestContainer {

    private GroovyScriptEngine groovyScriptEngine;
    private ObjectMapper objectMapper;


    public GroovyTestContainer() {
        super();
        objectMapper = new ObjectMapper();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(FOLDER_DIR);
        String path = url.getPath();

        SCRIPT_ROOT_PATH = path;

        String[] urls = new String[]{SCRIPT_ROOT_PATH};
        try {
            groovyScriptEngine = new GroovyScriptEngine(urls);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<File> loadScripts() throws TestCotainerBuildException {
        List<File> scriptsToEvaluate = new ArrayList<>();

        File[] files = new File(SCRIPT_ROOT_PATH).listFiles();

        for (File file : files) {
            String fileName = file.getName();
            boolean isDirectory = file.isDirectory();
            if (isDirectory  && !(fileName.equalsIgnoreCase(SCRIPT_TEST_DATA_DIR))) {
                File[] currentDirectoryFiles = file.listFiles();
                for (File currentDirectoryFile : currentDirectoryFiles) {
                    if (currentDirectoryFile.getName().matches(SCRIPT_REGEX)) {
                        //evaluateScript(currentDirectoryFile);
                        scriptsToEvaluate.add(currentDirectoryFile);
//                        System.out.println(currentDirectoryFile.getPath());
                    }
                }
            }
        }

        for(File script: scriptsToEvaluate) {
            testScript(script);
        }

        return scriptsToEvaluate;
    }

    @Override
    public boolean testScript(File scriptFile) throws TestCotainerBuildException {

        Map input = loadInput(scriptFile);
        String expectedOutput = loadOutput(scriptFile);
        String calculatedOutput = null;

        Binding binding = new Binding();
        binding.setProperty("dataMap", input);
        try {
            groovyScriptEngine.run(scriptFile.getPath(), binding);
            calculatedOutput = (String) binding.getProperty(CALCULATED_OUTPUT_VAR);
        } catch (ResourceException e) {
            e.printStackTrace();
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (MissingPropertyException e) {
            e.printStackTrace();
        }

        if (calculatedOutput==null){
            System.out.println("CALCULATED OUTPUT IS NULL: Groovy script execution error.");
            return false;
        }

        return Objects.equals(expectedOutput, calculatedOutput);
    }

    private String loadOutput(File scriptFile) {
        String fileName = stripExtension(scriptFile.getName());

        String outputPath = SCRIPT_ROOT_PATH +"/"
                +SCRIPT_TEST_DATA_DIR+"/"
                +SCRIPT_OUTPUT_DIR+"/"
                +scriptFile.getParentFile().getName()+"/"
                +fileName+".txt";

        File outputFile = new File(outputPath);
        String expectedOutput = readFile(outputFile);

        return expectedOutput;
    }

    private String stripExtension(String s)
    {
        return s != null && s.lastIndexOf(".") > 0 ? s.substring(0, s.lastIndexOf(".")) : s;
    }

    private Map loadInput(File scriptFile) throws TestCotainerBuildException {
        String fileName = stripExtension(scriptFile.getName());

        String inputPath = SCRIPT_ROOT_PATH +"/"
                +SCRIPT_TEST_DATA_DIR+"/"
                +SCRIPT_INPUT_DIR+"/"
                +scriptFile.getParentFile().getName()+"/"
                +fileName+".json";

        File inputFile = new File(inputPath);
//        String inputFileContents = readFile(inputFile);

        Map<String, Object> dataMap = new HashMap<>();
        try {
            Map inputMap = objectMapper.readValue(inputFile, Map.class);
            String inputType = inputMap.get("inputType").toString();
            String inputKey = inputMap.get("inputKey").toString();
            Object inputValue = inputMap.get("inputValue");
            Object data = null;

            switch (inputType.toLowerCase()) {
                case JSON_TYPE: {
                    data = objectMapper.convertValue(inputValue, Map.class);
                    break;
                }
                case STRING_TYPE: {

                }
                default: {
                    data = inputValue.toString();
                }
            }

            dataMap.put(inputKey, data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new TestCotainerBuildException("File Not Found: Please provide input sample data file");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMap;
    }

    private String readFile(File file) {
        LineIterator it = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            it = FileUtils.lineIterator(file, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while (it.hasNext()) {
                String line = it.nextLine();
                // do something with line
                stringBuilder.append(line);
            }
        } finally {
            LineIterator.closeQuietly(it);
        }

        return stringBuilder.toString();
    }

    @Deprecated
    private String readFileWithBuffer(File file) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            reader = new BufferedReader(new FileReader(file));
            char[] buffer = new char[10];
             while (reader.read(buffer) != -1) {
                 stringBuilder.append(new String(buffer));
                 buffer = new char[10];
             }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = stringBuilder.toString();
        return content;
    }


}
