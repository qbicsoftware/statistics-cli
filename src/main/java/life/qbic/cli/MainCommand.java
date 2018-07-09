package life.qbic.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Abstraction of command-line arguments that will be passed to {@link MainTool} at construction time.
 */
@Command(
   name="Main",
   description="This tool  is responsible for data retrieval and formatting, in order to visualize data on our homepage.")
public class MainCommand extends AbstractCommand {
    // TODO: add your command-line options as members of this class using picocli's annotations, for instance:
    @Option(names = {"-h", "--help"}, description = "display a help message", usageHelp = true)
    private boolean help;

    @Option(names = {"-url"}, description = "OpenBIS URL to access", required = true)
    private String openBISUrl;

    @Option(names = {"-u", "--user"}, description = "OpenBIS access username", required = true)
    private String openBISUsername;

    @Option(names = {"-p", "--password"}, description = "OpenBIS login password", required = true)
    private String openBISPassword;

    @Option(names = {"-o", "--outputFile"}, description = "Name of the output file", required = true) //I guess this isn't mandatory? we could provide a default option
    private String outputFileName;

    public String getOpenBISUrl() {
        return openBISUrl;
    }

    public String getOpenBISUsername() {
        return openBISUsername;
    }

    public String getOpenBISPassword() {
        return openBISPassword;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public boolean isHelp() {
        return help;
    }
}
