package life.qbic.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Abstraction of command-line arguments that will be passed to {@link MainTool} at construction time.
 */
@Command(
        name = "Statistics-CLI",
        description = "This tool  is responsible for data retrieval and formatting, in order to visualize data on our homepage.")
public class MainCommand extends AbstractCommand {
    //TODO: add your command-line options as members of this class using picocli's annotations, for instance:
    @Option(names = {"-openBisUrl"}, description = "OpenBIS URL to access", required = true)
    public String openBISUrl;

    @Option(names = {"-user"}, description = "OpenBIS access username", required = true)
    public String openBISUsername;

    @Option(names = {"-password"}, description = "OpenBIS login password", required = true)
    public String openBISPassword;

    @Option(names = {"-outputFile"}, description = "Name of the output file", required = true)
    public String outputFileName;

    @Option(names = {"-ncbiTaxUrl"}, description = "REST Link to NCBI Taxonomy DB", required = true)
    public String ncbiTaxUrl;

    @Option(names = {"-domainThreshold"}, description = "Threshold in percent of when a species is displayed among the " +
            "domains (Eukaryota, Bacteria,...) and not in a domain subchart (Eukaryota: Mus musculus,...).", required = true)
    public double domainThreshold;

    @Option(names = {"-gitHubUrl"}, description = "GitHub link to organization containing workflow repos", required = true)
    public String gitHubUrl;

    //Header parameters are required because the API for repos access is still under development
    @Option(names = {"-gitHubHeaderKey"}, description = "GitHub link header key", required = true)
    public String gitHubHeaderKey;

    @Option(names = {"-gitHubHeaderValue"}, description = "GitHub link header value", required = true)
    public String gitHubHeaderValue;

    @Option(names = {"-maxNumRepos"}, description = "Maximum of repos in the given organization that are searched for workflow tags." +
            "This is just to be extra safe that the retrieval loop is not executed infinitely in case the empty page signature of GitHub API changes.", required = true)
    public int maxNumRepos;
}
