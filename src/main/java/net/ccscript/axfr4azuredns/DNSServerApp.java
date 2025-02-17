package net.ccscript.axfr4azuredns;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.ccscript.axfr4azuredns.server.DNSServer;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfigurationException;

/**
 * Parses arguments and launches the DNS Server Application.
 */
public final class DNSServerApp {

    private static Option helpOption = Option.builder("h")
        .longOpt("help")
        .desc("Prints this help message")
        .build();

    private static Logger logger = LogManager.getLogger();

    /**
     * Private constructor: this class only has one static public method: {@link #main(String[])}.
     */
    private DNSServerApp() {
    }

    /**
     * Starts the DNS Slave Server.
     * @param args The arguments of the program:
     *      [-h|-help]                  Print help message and discards the rest
     *      [-c|-config] config_file    The JSON configuration file. Mandatory to start the server.
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException, DNSServerConfigurationException {
        if (asksForHelp(args)) {
            return;
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmdArguments;

        try {
            cmdArguments = parser.parse(getCommandLineOptions(), args, false);
        } catch (MissingOptionException moe) {
            throw new IllegalArgumentException(
                "Configuration file is mandatory (-c config_file). Use -h to get help.", moe);
        }

        String configurationFileName = cmdArguments.getOptionValue("c");
        logger.info("Configuration file is in: {}", configurationFileName);

        DNSServer dnsServer = new DNSServer(configurationFileName);
        dnsServer.start();
    }

    /**
     * Checks if the command line arguments are asking for help. If so, display the help message.
     * @param args the command line arguments
     * @return true if the command line was asking for help, false otherwise
     * @throws ParseException
     */
    private static boolean asksForHelp(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        Options clOptions = new Options();
        clOptions.addOption(helpOption);

        CommandLine cmdArguments = parser.parse(clOptions, args, true);

        if (cmdArguments.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("axfr4azuredns", getCommandLineOptions());
            return true;
        }

        return false;
    }

    /**
     * Builds the Command Line {@link Options} objects.
     * @return the command line {@link Options} supported by the application.
     */
    private static Options getCommandLineOptions() {
        Options clOptions = new Options();

        clOptions.addOption(helpOption);

        Option configurationFileOption = Option.builder("c")
            .longOpt("config")
            .argName("config_file")
            .hasArg()
            .required()
            .desc("The JSON configuration file")
            .build();
        clOptions.addOption(configurationFileOption);

        return clOptions;
    }

}
