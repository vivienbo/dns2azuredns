package net.ccscript.axfr4azuredns.server.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.AzureCredentials;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.AzureDomain;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.DNSDomain;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.DNSSynchronizationMaster;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.Server;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.Zone;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.ZoneTransferType;
import net.ccscript.axfr4azuredns.server.security.AllowAllDNSServerAccessRightManager;
import net.ccscript.axfr4azuredns.server.security.DNSAccessRight;
import net.ccscript.axfr4azuredns.server.security.DNSAccessRightSet;

public class DNSServerConfigurationFactoryTest {

    private static final int TEST_ONE_PORT = 53;
    private static final int TEST_ONE_POLLPERIOD = 5;
    private static final ZoneTransferType TEST_ONE_TXMODE = ZoneTransferType.IXFR;
    private static final byte[] LOCAL_HOST_BYTES = new byte[]{127, 0, 0, 1};

    @Test
    void testFullValidConfigurationServersContents() throws FileNotFoundException,
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
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        DNSServerConfiguration testConfigOne =
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);

        assertEquals(testConfigOne.getServers().size(), 1);
        for (Server server : testConfigOne.getServers()) {
            assertEquals(server.getListenOn(), "127.0.0.1");
            assertEquals(server.getTcpPort(), TEST_ONE_PORT);
            assertTrue(server.isUdpEnabled());
            assertEquals(server.getUdpPort(), TEST_ONE_PORT);
        }

        assertEquals(testConfigOne.getAzureCredentials().size(), 1);
        for (AzureCredentials credentials : testConfigOne.getAzureCredentials()) {
            assertEquals(credentials.getTenant(), "exampletenant.onmicrosoft.com");
            assertEquals(credentials.getServicePrincipal(), "00000000-1111-4444-2222-333333333333");
            assertEquals(credentials.getPassword(), "passw0rd");
        }

        assertEquals(testConfigOne.getZones().size(), 1);
        for (Zone zone : testConfigOne.getZones()) {
            assertEquals(zone.getZoneName(), "sub.example.com");
            assertEquals(zone.getDnsDomainConfiguration().getDnsServersIPs().size(), 2);
            assertEquals(zone, testConfigOne.getZoneByName("sub.example.com"));

            DNSDomain dnsDomain = zone.getDnsDomainConfiguration();
            Iterator<String> dnsIPs = dnsDomain.getDnsServersIPs().iterator();
            String ip1 = dnsIPs.next();
            assertEquals(ip1, "192.168.100.1");
            String ip2 = dnsIPs.next();
            assertEquals(ip2, "192.168.100.254");
            assertEquals(dnsDomain.getZoneTransfer(), ZoneTransferType.IXFR);
            assertEquals(dnsDomain.getPollingInterval(), TEST_ONE_POLLPERIOD);

            AzureDomain azureDomain = zone.getAzureDomain();
            assertEquals(azureDomain.getAzureResourceGroup(), "AMDProject_DNS_Global");
            assertEquals(azureDomain.getAzureZoneName(), "example.com");
            assertEquals(azureDomain.getAzureServicePrincipal(), "00000000-1111-4444-2222-333333333333");
            assertEquals(azureDomain.getAzureSubscription(), "55555555-6666-4444-7777-888888888888");
        }
    }

    @Test
    void testInvalidJSONConfigurationServersContents() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {
        String json = "{a:n}";
        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });
    }

    @Test
    void testLoadJSONConfigurationFromFile() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String fileName = "conf/example.json";
        DNSServerConfigurationFactory.createDNSServerConfigurationFromFile(fileName);
    }

    @Test
    void testLoadJSONConfigurationFromStream() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String fileName = "conf/example.json";
        InputStream inputStream = new FileInputStream(fileName);
        DNSServerConfigurationFactory.createDNSServerConfigurationFromStream(inputStream);
    }

    @Test
    void testInvalidListenIP() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"a.b.c.d\",\"tcp_port\":53,\"udp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });
    }

    @Test
    void testNoUDPPortAndDefaults() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\"}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"]},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        DNSServerConfiguration configuration = DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        for (Server server : configuration.getServers()) {
            assertFalse(server.isUdpEnabled());
            assertThrows(DNSServerConfigurationException.class, () -> {
                server.getUdpPort();
            });
            assertEquals(server.getTcpPort(), TEST_ONE_PORT);
        }
        Zone testZone = configuration.getZoneByName("sub.example.com");
        assertEquals(testZone.getDnsDomainConfiguration().getPollingInterval(), TEST_ONE_POLLPERIOD);
        assertEquals(testZone.getDnsDomainConfiguration().getZoneTransfer(), TEST_ONE_TXMODE);
        assertEquals(testZone.getMaster(), DNSSynchronizationMaster.DNS);
    }

    @Test
    void testInvalidDNSIP() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"256.256.256.256\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });
    }

    @Test
    void testInvalidServicePrincipal() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"ggabdc\",\"password\":\"passw0rd\"}]}";

        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });
    }

    @Test
    void testInvalidServicePrincipalForAzureDNSZone() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"12345678-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });
    }

    @Test
    void testInvalidAzureZoneName() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.zz\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });
    }

    @Test
    void testAzureDNSMismatch() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"sub.example2.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        assertThrows(DNSServerConfigurationException.class, () -> {
            DNSServerConfigurationFactory.createDNSServerConfiguration(json);
        });

        String json2 = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"example.com\",\"master\": \"dns\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"sub.example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        DNSServerConfiguration config = DNSServerConfigurationFactory.createDNSServerConfiguration(json2);
        assertEquals(config.getZoneByName("example.com").getAzureDomain().getAzureZoneName(), "sub.example.com");
    }

    @Test
    void testAzureMaster() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json2 = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"example.com\",\"master\": \"azure\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"sub.example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        DNSServerConfiguration config = DNSServerConfigurationFactory.createDNSServerConfiguration(json2);
        assertEquals(config.getZoneByName("example.com").getMaster(), DNSSynchronizationMaster.AZURE);
    }

    @Test
    void testOptionsSecurityManager() throws FileNotFoundException,
        IOException, DNSServerConfigurationException {

        String json2 = "{"
            + "\"servers\":[{\"listen_on\":\"127.0.0.1\",\"tcp_port\":53}],"
            + "\"zones\":[{\"zone_name\":\"example.com\",\"master\": \"azure\","
            + "\"dns\":{\"servers\":[\"192.168.100.1\","
            + "\"192.168.100.254\"],\"zone_transfer\":\"ixfr\",\"polling_interval\":5},"
            + "\"azure\":{\"zone_name\":\"sub.example.com\",\"resourcegroup\":\"AMDProject_DNS_Global\","
            + "\"subscription\":\"55555555-6666-4444-7777-888888888888\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\"}}],"
            + "\"azure_credentials\":[{\"tenant\":\"exampletenant.onmicrosoft.com\","
            + "\"service_principal\":\"00000000-1111-4444-2222-333333333333\",\"password\":\"passw0rd\"}]}";

        DNSServerConfiguration config = DNSServerConfigurationFactory.createDNSServerConfiguration(json2);

        InetAddress localhost = InetAddress.getByAddress(LOCAL_HOST_BYTES);

        // If the assertion is false, it's likely the unit test has to be upgraded
        assertEquals(config.getOptions().getAccessRightManager().getClass(), AllowAllDNSServerAccessRightManager.class);

        assertTrue(config.getOptions().getAccessRightManager().isAllowed(localhost));
        assertTrue(config.getOptions().getAccessRightManager().getAccessRightSet(
            localhost, "example.com").canQueryDomainNameTransfer());
        assertTrue(config.getOptions().getAccessRightManager().getAccessRightSet(
            localhost, "example.com").canQueryDomainName());

        DNSAccessRightSet rightsSet = config.getOptions().getAccessRightManager().getAccessRightSet(
            localhost, "example.com");
        DNSAccessRightSet newRights = new DNSAccessRightSet();
        newRights.addRight(DNSAccessRight.DENY);
        rightsSet.addRights(newRights);
        assertFalse(rightsSet.canQueryDomainNameTransfer());
        assertFalse(rightsSet.canQueryDomainName());

        rightsSet.removeRight(DNSAccessRight.DENY);
        assertTrue(rightsSet.canQueryDomainNameTransfer());
        assertTrue(rightsSet.canQueryDomainName());

        DNSAccessRightSet emptyRights = new DNSAccessRightSet();
        assertFalse(emptyRights.canQueryDomainNameTransfer());
        assertFalse(emptyRights.canQueryDomainName());

        DNSAccessRightSet combinedSet = DNSAccessRightSet.combineDnsAccessRightSet(rightsSet, newRights);
        assertFalse(combinedSet.canQueryDomainName());
        assertFalse(combinedSet.canQueryDomainNameTransfer());
    }
}
