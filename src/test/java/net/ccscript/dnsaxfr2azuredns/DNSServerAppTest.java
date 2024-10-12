package net.ccscript.dnsaxfr2azuredns;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.jupiter.api.Test;

import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfigurationException;

public class DNSServerAppTest {

    private static final String[] GET_HELP_REFERENCE = new String[]{
        "usage: dnsaxfr2azuredns",
        " -c,--config <config_file>   The JSON configuration file",
        " -h,--help                   Prints this help message"
    };

    @Test
    void testMainWithoutArgument() {
        assertThrows(IllegalArgumentException.class, () -> {
            runMainWithoutArgument();
        });

        try {
            runMainWithoutArgument();
        } catch (IllegalArgumentException iae) {
            assertEquals(iae.getMessage(), "Configuration file is mandatory (-c config_file). Use -h to get help.");
            assertEquals(MissingOptionException.class, iae.getCause().getClass());
        } catch (ParseException pe) {
            assertTrue(false);
        } catch (DNSServerConfigurationException dsce) {
            assertTrue(false);
        }
    }

    private void runMainWithoutArgument() throws ParseException, DNSServerConfigurationException {
        String[] emptyArguments = new String[]{};
        DNSServerApp.main(emptyArguments);
    }

    private void runMainAskingUnknownOption() throws ParseException, DNSServerConfigurationException {
        String[] wrongArguments = new String[]{
            "-z"
        };
        DNSServerApp.main(wrongArguments);
    }

    @Test
    void testMainWithWrongArgument() {
        try {
            runMainAskingUnknownOption();
        } catch (DNSServerConfigurationException dsce) {
            assertTrue(false);
        } catch (ParseException pe) {
            assertEquals(UnrecognizedOptionException.class, pe.getClass());
            assertEquals(pe.getMessage(), "Unrecognized option: -z");
        }
    }

    private void runMainAskingHelp() throws ParseException, DNSServerConfigurationException {
        String[] helpArguments = new String[] {
            "-h"
        };
        DNSServerApp.main(helpArguments);
    }

    @Test
    void testMainWithHelp() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        System.setOut(new PrintStream(outContent));
        try {
            runMainAskingHelp();
        } catch (ParseException pe) {
            assertTrue(false);
        } catch (DNSServerConfigurationException dsce) {
            assertTrue(false);
        }

        String whatWasPrinted = new String(outContent.toByteArray());
        String[] linesOfOutput = whatWasPrinted.split(
            System.getProperty("line.separator")
        );

        assertEquals(linesOfOutput.length, GET_HELP_REFERENCE.length);
        assertEquals(linesOfOutput[0], GET_HELP_REFERENCE[0]);
        assertEquals(linesOfOutput[1], GET_HELP_REFERENCE[1]);
        assertEquals(linesOfOutput[2], GET_HELP_REFERENCE[2]);

        System.setOut(originalOut);
    }

    private void runMainFakeConfig() throws ParseException, DNSServerConfigurationException {
        String[] helpArguments = new String[] {
            "-c",
            "/conf/failtest.json"
        };
        DNSServerApp.main(helpArguments);
    }

    @Test
    void testMainWithFakeConfig() {
        assertThrows(DNSServerConfigurationException.class, () -> {
            runMainFakeConfig();
        });
    }

}
