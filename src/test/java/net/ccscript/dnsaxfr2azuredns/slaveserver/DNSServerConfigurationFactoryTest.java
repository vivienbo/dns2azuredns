package net.ccscript.dnsaxfr2azuredns.slaveserver;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfiguration;
import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfigurationException;
import net.ccscript.dnsaxfr2azuredns.server.configuration.DNSServerConfigurationFactory;

public class DNSServerConfigurationFactoryTest {

    @Test
    void testCheckConfigurationServersContents() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53,\"udp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"password\"}]}";

        DNSServerConfiguration testConfigOne =
            DNSServerConfigurationFactory.createDNSSlaveServerConfiguration(json);

        //assertEquals(DNSSlaveServerConfigurationFactory.createDNSSlaveServerConfiguration(null), 1);
        assertEquals(testConfigOne.getServers().size(), 1);
    }

}
