package io.writer;

import model.data.MainConfig;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author fhanssen
 * Writes given MainConfig to file. DON'T change the DumperOptions, unless you are absolutely sure of what you are doing.
 * This has to be synced with the Reader in the statistics portlet.
 */
public final class YAMLWriter {

    public static void writeToFile(String outputFile, MainConfig config){
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try {
            FileWriter writer = new FileWriter(outputFile);
            yaml.dump(config, writer);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
