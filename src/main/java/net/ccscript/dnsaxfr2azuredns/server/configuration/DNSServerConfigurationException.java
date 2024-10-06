package net.ccscript.dnsaxfr2azuredns.server.configuration;

/**
 * Used to report exceptions on the DNSSlaveServerConfiguration.
 */
public class DNSServerConfigurationException extends Exception {

    public DNSServerConfigurationException(String message, Exception exception) {
        super(message, exception);
    }

    public DNSServerConfigurationException(String message) {
        super(message);
    }

}
