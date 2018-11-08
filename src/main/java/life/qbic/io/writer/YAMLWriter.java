package life.qbic.io.writer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import submodule.data.MainConfig;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author fhanssen
 * Writes given MainConfig to file. DON'T change the DumperOptions, unless you are absolutely sure of what you are doing.
 * This has to be synced with the Reader in the statistics portlet.
 */
public final class YAMLWriter {

    private static final Logger logger = LogManager.getLogger(YAMLWriter.class);

    public static void writeToFile(String outputFile, MainConfig config){
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try {
            FileWriter writer = new FileWriter(outputFile);
            yaml.dump(config, writer);
            logger.info("Charts were successfully written to config");
        }catch(IOException e){
            e.printStackTrace();
            logger.error("Config file could not be written: " + e.getMessage());
        }
    }
}
