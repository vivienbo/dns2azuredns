package net.ccscript.axfr4azuredns;

import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import net.ccscript.axfr4azuredns.i18n.Axfr4AzureDnsText;
import net.ccscript.axfr4azuredns.server.DNSServer;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfigurationException;

import ch.qos.cal10n.MessageConveyor;

/**
 * Parses arguments and launches the DNS Server Application.
 */
public final class DNSServerApp {

    private static MessageConveyor i18n = new MessageConveyor(Locale.ENGLISH);
    private static LocLogger logger = new LocLoggerFactory(i18n).getLocLogger(DNSServerApp.class);

    private static Option helpOption = Option.builder("h")
        .longOpt("help")
        .desc(i18n.getMessage(Axfr4AzureDnsText.APP_HELP_MESSAGE))
        .build();

    /**
     * Private constructor: this class only has one static public method: {@link #main(String[])}.
     */
    private DNSServerApp() {
    }

    /**
     * Starts the DNS Server.
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
                i18n.getMessage(Axfr4AzureDnsText.APP_ERROR_MISSING_CONFIG), moe);
        }

        String configurationFileName = cmdArguments.getOptionValue("c");
        logger.info(Axfr4AzureDnsText.APP_LOGGER_CONFIG_LOCATION, configurationFileName);

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
            formatter.printHelp(i18n.getMessage(Axfr4AzureDnsText.APP_HELP_APPNAME), getCommandLineOptions());
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
            .desc(i18n.getMessage(Axfr4AzureDnsText.APP_CONFIG_MSG))
            .build();
        clOptions.addOption(configurationFileOption);

        return clOptions;
    }

}
