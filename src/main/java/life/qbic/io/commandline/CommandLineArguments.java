package life.qbic.io.commandline;

import picocli.CommandLine.Option;

public class CommandLineArguments {

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
