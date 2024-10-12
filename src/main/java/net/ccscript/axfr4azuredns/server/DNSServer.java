package net.ccscript.axfr4azuredns.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfigurationException;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfigurationFactory;

/**
 * A DNS Server and all processing logic multithreading logic associated.
 */
public class DNSServer {

    private static Logger logger = LogManager.getLogger();
    private static ResourceBundle i18n =
        ResourceBundle.getBundle("net.ccscript.axfr4azuredns.server.i18n",
            Locale.getDefault());

    private DNSServerConfiguration configuration;

    /**
     * Creates a {@link DNSServer} based on a given configuration JSON.
     * @param configurationFileName The location of the configuration JSON.
     * @throws DNSServerConfigurationException In case configuration could not be read.
     */
    public DNSServer(String configurationFileName) throws DNSServerConfigurationException {
        try {
            logger.info(i18n.getString("server.logger.configurationlocation"), configurationFileName);
            configuration = DNSServerConfigurationFactory.createDNSServerConfigurationFromFile(
                configurationFileName);
        } catch (FileNotFoundException e) {
            throw new DNSServerConfigurationException(i18n.getString("server.exception.configfilenotfound"), e);
        } catch (IOException e) {
            throw new DNSServerConfigurationException(i18n.getString("server.exception.configerror"), e);
        } catch (DNSServerConfigurationException e) {
            throw e;
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
                logger.warn(i18n.getString("server.logger.shutdownhook"));
                thisServer.stop();
            }
        });
    }

    public void stop() {
        // TODO stops the server threads.
    }

}
