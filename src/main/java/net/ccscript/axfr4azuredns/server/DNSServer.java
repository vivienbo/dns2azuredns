package net.ccscript.axfr4azuredns.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import net.ccscript.axfr4azuredns.DNSServerApp;
import net.ccscript.axfr4azuredns.i18n.ServerText;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfigurationException;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfigurationFactory;

import ch.qos.cal10n.MessageConveyor;

/**
 * A DNS Server and all processing logic multithreading logic associated.
 */
public class DNSServer {

    private static MessageConveyor i18n = new MessageConveyor(Locale.ENGLISH);
    private static LocLogger logger = new LocLoggerFactory(i18n).getLocLogger(DNSServerApp.class);

    private DNSServerConfiguration configuration;

    /**
     * Creates a {@link DNSServer} based on a given configuration JSON.
     * @param configurationFileName The location of the configuration JSON.
     * @throws DNSServerConfigurationException In case configuration could not be read.
     */
    public DNSServer(String configurationFileName) throws DNSServerConfigurationException {
        try {
            logger.info(ServerText.SERVER_LOGGER_CONFIGURATIONLOCATION, configurationFileName);
            configuration = DNSServerConfigurationFactory.createDNSServerConfigurationFromFile(
                configurationFileName);
        } catch (FileNotFoundException e) {
            throw new DNSServerConfigurationException(
                i18n.getMessage(ServerText.SERVER_EXCEPTION_CONFIGFILENOTFOUND), e
            );
        } catch (IOException e) {
            throw new DNSServerConfigurationException(i18n.getMessage(ServerText.SERVER_EXCEPTION_CONFIGERROR), e);
        } catch (DNSServerConfigurationException e) {
            throw new DNSServerConfigurationException(i18n.getMessage(ServerText.SERVER_EXCEPTION_CONFIGERROR), e);
        }
        addShutdownHook();
    }

    /**
     * Starts the server and configures a SIGNAL listener in case of stop order.
     */
    public void start() {
        // TODO start Signal listener in case of termination
        // TODO start server listener thread(s)
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ie) {
        }
    }

    private void addShutdownHook() {
        DNSServer thisServer = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.warn(ServerText.SERVER_LOGGER_SHUTDOWNHOOK_TRIGGERED);
                thisServer.stop();
                System.out.println(i18n.getMessage(ServerText.SERVER_LOGGER_STOP));
            }
        });
        logger.info(ServerText.SERVER_LOGGER_SHUTDOWNHOOK_ADDED);
    }

    public void stop() {
        // TODO stops the server threads.
    }

}
