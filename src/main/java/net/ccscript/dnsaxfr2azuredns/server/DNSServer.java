package net.ccscript.dnsaxfr2azuredns.server;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfiguration;
import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfigurationException;
import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfigurationFactory;

/**
 * A DNS Slave Server.
 */
public class DNSServer {

    private Logger logger = LogManager.getLogger();
    private DNSServerConfiguration configuration;

    /**
     * Creates a {@link #DNSSlaveServer} based on a given configuration JSON.
     * @param configurationFileName The location of the configuration JSON.
     * @throws DNSServerConfigurationException In case configuration could not be read.
     */
    public DNSServer(String configurationFileName) throws DNSServerConfigurationException {
        try {
            logger.info("DNS Server is loading configuration from file {}", configurationFileName);
            configuration = DNSServerConfigurationFactory.createDNSServerConfigurationFromFile(
                configurationFileName);
        } catch (FileNotFoundException e) {
            throw new DNSServerConfigurationException("Configuration file could not be found", e);
        } catch (IOException e) {
            throw new DNSServerConfigurationException("Error reading schema or configuration", e);
        } catch (DNSServerConfigurationException e) {
            throw e;
        }
    }

    /**
     * Starts the server and configures a SIGNAL listener in case of stop order.
     */
    public void start() {
        // TODO start Signal listener in case of termination
        // TODO start server listener thread(s)
    }

    public void stop() {
        // TODO stops the server threads.
    }

}
