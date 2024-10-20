package net.ccscript.axfr4azuredns.server.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

import net.ccscript.axfr4azuredns.DNSServerApp;
import net.ccscript.axfr4azuredns.i18n.ConfigurationText;
import net.ccscript.axfr4azuredns.server.DNSServer;
import net.ccscript.axfr4azuredns.server.security.AllowAllDNSServerAccessRightManager;
import net.ccscript.axfr4azuredns.server.security.DNSServerAccessRightManager;

import ch.qos.cal10n.MessageConveyor;

/**
 * The base configuration object for the {@link DNSServer}.
 */
public final class DNSServerConfiguration {

    private static MessageConveyor i18n = new MessageConveyor(Locale.ENGLISH);
    private static LocLogger logger = new LocLoggerFactory(i18n).getLocLogger(DNSServerApp.class);

    private Set<Server> servers;
    private Map<String, Zone> zones;
    private Map<String, AzureCredentials> azureCredentials;
    private Options options;

    /**
     * Initialize the Sets and Collections used for Server Configuration.
     * This returns an empty configuration to be populated.
     */
    DNSServerConfiguration() {
        this.servers = new HashSet<Server>();
        this.zones = new HashMap<String, Zone>();
        this.azureCredentials = new HashMap<String, AzureCredentials>();
        this.options = new Options();
    }

    /**
     * Adds a {@link Server} to the collection of Servers.
     * @param server the {@link Server} to be added to the collection.
     * @throws DNSServerConfigurationException if the server listenOn property isn't a valid IP address
     */
    void addServer(Server server) throws DNSServerConfigurationException {
        this.servers.add(server);
    }

    /**
     * Gets the list of {@link Server}s to be launched.
     * @return the list of {@link Server}s.
     */
    public Set<Server> getServers() {
        return this.servers;
    }

    /**
     * Adds a {@link Zone} to the collection of Domains to be replicated.
     * At the time of addition, this will check if an Azure Credentials corresponds to this entry.
     * Therefore, Azure Credentials must be added **before** calling addZone.
     * @param zone the {@link Zone} to be added to the collection.
     * @throws DNSServerConfigurationException If the provided Zone refers to a ServicePrincipal
     *                                         that does not exist in AzureCredentials.
     */
    void addZone(Zone zone) throws DNSServerConfigurationException {
        String servicePrincipal = zone.getAzureDomain().getAzureServicePrincipal();
        if (null == getAzureCredential(servicePrincipal)) {
            logger.error(ConfigurationText.CONF_ZONE_SERVICEPRINCIPALERROR_LOG,
                zone.getZoneName(), servicePrincipal
            );
            throw new DNSServerConfigurationException(
                i18n.getMessage(ConfigurationText.CONF_ZONE_SERVICEPRINCIPALERROR_EXCEPTION, servicePrincipal)
            );
        }
        this.zones.put(zone.getZoneName(), zone);
    }

    /**
     * Gets the collection of {@link Zone}s to be replicated.
     * @return an unmodifiable collection of {@link Zone}s
     */
    public Collection<Zone> getZones() {
        return Collections.unmodifiableCollection(this.zones.values());
    }

    /**
     * Gets the {@link Zone} object for a given DNS Zone Name.
     * @param zoneName the DNS Zone Name.
     * @return the {@link Zone} object or null if the DNS Zone Name was not found.
     */
    public Zone getZoneByName(String zoneName) {
        return this.zones.get(zoneName);
    }

    /**
     * Adds an {@link AzureCredentials} to the known credentials for this project.
     * @param singleAzureCredentials the {@link AzureCredentials} to be created.
     */
    void addAzureCredentials(AzureCredentials singleAzureCredentials) {
        this.azureCredentials.put(singleAzureCredentials.getServicePrincipal(), singleAzureCredentials);
    }

    /**
     * Gets the collection of {@link AzureCredentials} that are available.
     * @return an unmodifiable collection of {@link AzureCredentials}.
     */
    public Collection<AzureCredentials> getAzureCredentials() {
        return Collections.unmodifiableCollection(this.azureCredentials.values());
    }

    /**
     * Gets the {@link AzureCredentials} object for a given DNS Zone Name.
     * @param servicePrincipal the service principal for which credentials needs to be retrieved.
     * @return the corresponding {@link AzureCredentials} or null if the Service Principal was not found.
     */
    public AzureCredentials getAzureCredential(String servicePrincipal) {
        return this.azureCredentials.get(servicePrincipal);
    }

    public Options getOptions() {
        return this.options;
    }

    public final class Options {

        private DNSServerAccessRightManager accessRightManager =
            new AllowAllDNSServerAccessRightManager();

        public DNSServerAccessRightManager getAccessRightManager() {
            return accessRightManager;
        }

    }

    /**
     * Server Configuration Object.
     */
    public final class Server {
        /**
         * The default TCP port for the DNS Server. 53 by default.
         */
        public static final int DEFAULT_TCP_PORT = 53;
        /**
         * The default UDP port for the DNS Server. Disabled by default (0).
         */
        public static final int DEFAULT_UDP_PORT = 0;

        private String listenOn;
        private int tcpPort;
        private int udpPort;

        /**
         * Creates a Server object.
         * @param listenOn the IPv4 or IPv6 on which the server must listen.
         * @param tcpPort the TCP port on which the server must listen. Mandatory value between 1 and 65535.
         * @param udpPort the UDP port on which the server must listen.
         *                0 if disabled. Otherwise between 1 and 65535 if activated.
         * @throws DNSServerConfigurationException if the listenOn parameter isn't a valid IP address.
         */
        Server(String listenOn, int tcpPort, int udpPort) throws DNSServerConfigurationException {
            setListenOn(listenOn);
            this.tcpPort = tcpPort;
            this.udpPort = udpPort;
        }

        /**
         * Sets the listenOn address and checks this is a valid IP address.
         * @param listenOn the IP address to listen to.
         * @throws DNSServerConfigurationException if the listenOn parameter isn't a valid IP address.
         */
        private void setListenOn(String listenOn) throws DNSServerConfigurationException {
            InetAddressValidator ipAddressValidator = InetAddressValidator.getInstance();

            if (!ipAddressValidator.isValid(listenOn)) {
                logger.error(ConfigurationText.CONF_SERVER_LISTENONERROR, listenOn);
                throw new DNSServerConfigurationException(
                    i18n.getMessage(ConfigurationText.CONF_SERVER_LISTENONERROR, listenOn)
                );
            }

            this.listenOn = listenOn;
        }

        /**
         * Gets the IP address on which the server should listen.
         * @return the IP address on which the server should listen (IPv4 or IPv6).
         */
        public String getListenOn() {
            return this.listenOn;
        }

        /**
         * Gets the TCP port on which the server should listen.
         * @return the TCP port on which the server should listen.
         */
        public int getTcpPort() {
            return this.tcpPort;
        }

        /**
         * Checks if UDP is enabled.
         * @return true if UDP is enabled. False otherwise.
         */
        public boolean isUdpEnabled() {
            return this.udpPort > 0;
        }

        /**
         * Gets the UDP port on which the server should listen.
         * @return the UDP port on which the server should listen.
         * @throws DNSServerConfigurationException if UDP is disabled for this server (see {@link isUdpEnabled}).
         */
        public int getUdpPort() throws DNSServerConfigurationException {
            if (!isUdpEnabled()) {
                throw new DNSServerConfigurationException(
                    i18n.getMessage(ConfigurationText.CONF_SERVER_UDPDISABLEDERROR)
                );
            }
            return this.udpPort;
        }
    }

    /**
     * The server which is used as master: DNS or AZURE.
     */
    public enum DNSSynchronizationMaster {
        /**
         * DNS is the master and Azure is the slave.
         */
        DNS,
        /**
         * Azure is the master and DNS is the slave.
         * */
        AZURE
    }

    /**
     * Domain name related Configuration.
     */
    public final class Zone {
        private String zoneName;
        private DNSSynchronizationMaster master;
        private DNSDomain dnsDomainConfiguration;
        private AzureDomain azureDomain;

        Zone(String zoneName, DNSSynchronizationMaster master, DNSDomain dnsDomain, AzureDomain azureDomain)
            throws DNSServerConfigurationException {
            setZoneNameAndAzureDomain(zoneName, azureDomain);
            this.dnsDomainConfiguration = dnsDomain;
            this.master = master;
        }

        /**
         * Sets the zoneName an AzureDomain while checking that zoneName is the AzureDomain's
         * zoneName or a subdomain of it and vice versa.
         * @param newZoneName the zoneName to be set.
         * @param newAzureDomain the azureDomain to be set.
         * @throws DNSServerConfigurationException If the zone names are not a subdomain of each other.
         */
        private void setZoneNameAndAzureDomain(String newZoneName, AzureDomain newAzureDomain)
            throws DNSServerConfigurationException {

            DomainValidator validator = DomainValidator.getInstance();
            String azureZoneName = newAzureDomain.getAzureZoneName();
            if (validator.isValid(azureZoneName)) {
                if (!newZoneName.endsWith(azureZoneName) && !azureZoneName.endsWith(newZoneName)) {
                    logger.error(ConfigurationText.CONF_ZONE_SUBDOMAINERROR,
                        newZoneName, azureZoneName
                    );
                    throw new DNSServerConfigurationException(
                        i18n.getMessage(ConfigurationText.CONF_ZONE_SUBDOMAINERROR)
                    );
                }
            } else {
                logger.error(ConfigurationText.CONF_ZONE_INVALIDNAME, azureZoneName);
                throw new DNSServerConfigurationException(
                    i18n.getMessage(ConfigurationText.CONF_ZONE_INVALIDNAME, azureZoneName)
                );
            }

            this.zoneName = newZoneName;
            this.azureDomain = newAzureDomain;
        }

        /**
         * Gets the Azure DNS Zone access information.
         * @return the Azure DNS Zone access information.
         */
        public AzureDomain getAzureDomain() {
            return this.azureDomain;
        }

        /**
         * Gets the non-Azure DNS Zone access information.
         * @return the non-Azure DNS Zone access information.
         */
        public DNSDomain getDnsDomainConfiguration() {
            return this.dnsDomainConfiguration;
        }

        /**
         * Gets the DNS Zone name which needs replication.
         * @return the DNS Zone name to be replicated. Is the same or a subdomain of the Azure DNS Zone.
         */
        public String getZoneName() {
            return this.zoneName;
        }

        public DNSSynchronizationMaster getMaster() {
            return this.master;
        }
    }

    /**
     * The non-Azure DNS Zone configuration related to the Master DNS.
     */
    public final class DNSDomain {
        /**
         * Default polling interval in case no NOTIFY is received.
         */
        public static final int DEFAULT_POLLING_INTERVAL = 5;
        /**
         * Default Zone Transfer type to be used: IXFR, falls back to AXFR if not supported by target server.
         */
        public static final ZoneTransferType DEFAULT_ZONE_TRANSFER_TYPE = ZoneTransferType.IXFR;

        private Collection<String> dnsServersIPs;
        private ZoneTransferType zoneTransfer;
        private int pollingInterval = 5;

        /**
         * Creates a DNSDomain object.
         * @param dnsServersIPs a Collection of IP addresses to be used to reach others dns servers.
         * @param zoneTransfer the {@link ZoneTransferType} to be used for Zone Transfers (mandatory).
         * @param pollingInterval the interval with which to poll the master server(s) in case no NOTIFY is received.
         * @throws DNSServerConfigurationException If at least one of the dnsServerIP is not a valid IP address.
         */
        DNSDomain(Collection<String> dnsServersIPs, ZoneTransferType zoneTransfer, int pollingInterval)
            throws DNSServerConfigurationException {

            setDnsServersIPs(dnsServersIPs);
            this.dnsServersIPs = new ArrayList<String>(dnsServersIPs);
            this.zoneTransfer = zoneTransfer;
            this.pollingInterval = pollingInterval;
        }

        /**
         * Sets the collection of dnsServersIPs.
         * @param dnsServersIPs A Collection of IP addresses to be used to reach others dns servers.
         * @throws DNSServerConfigurationException If at least one of the dnsServerIP is not a valid IP address.
         */
        private void setDnsServersIPs(Collection<String> dnsServersIPs) throws DNSServerConfigurationException {
            InetAddressValidator ipAddressValidator = InetAddressValidator.getInstance();

            for (String ipAddress : dnsServersIPs) {
                if (!ipAddressValidator.isValid(ipAddress)) {
                    logger.error(ConfigurationText.CONF_DNSDOMAIN_INVALIDIP, ipAddress);
                    throw new DNSServerConfigurationException(
                        i18n.getMessage(ConfigurationText.CONF_DNSDOMAIN_INVALIDIP, ipAddress)
                    );
                }
            }

            this.dnsServersIPs = new ArrayList<String>(dnsServersIPs);
        }

        /**
         * Gets a Collection of master server IPs. No guarantee in the order used.
         * @return the collection of master server IPs.
         */
        public Collection<String> getDnsServersIPs() {
            return this.dnsServersIPs;
        }

        /**
         * Gets the applicable {@link ZoneTransferType} to be used by default.
         * @return the type of {@link ZoneTransferType} to be used by default.
         */
        public ZoneTransferType getZoneTransfer() {
            return this.zoneTransfer;
        }

        /**
         * Gets the applicable polling interval in minutes in case no NOTIFY is received from master.
         * @return the applicable polling interval in minutes.
         */
        public int getPollingInterval() {
            return this.pollingInterval;
        }
    }

    /**
     * Domain configuration from the Azure perspective.
     */
    public final class AzureDomain {
        private String azureZoneName;
        private String azureSubscription;
        private String azureResourceGroup;
        private String azureServicePrincipal;

        /**
         * Creates an AzureDomain, representing the Azure Zone to be synchronized with the non-Azure DNS.
         * @param azureZoneName the Azure DNS Zone.
         * @param azureSubscription the Azure subscription containing the Resource Group.
         * @param azureResourceGroup the Azure Resource Group to which belongs the Azure DNS Zone.
         * @param azureServicePrincipal the Azure Service Principal used to authenticate to manage the Azure DNS Zone.
         */
        AzureDomain(String azureZoneName, String azureSubscription,
            String azureResourceGroup, String azureServicePrincipal) throws DNSServerConfigurationException {

            this.azureZoneName = azureZoneName;
            this.azureSubscription = azureSubscription;
            this.azureResourceGroup = azureResourceGroup;
            this.azureServicePrincipal = azureServicePrincipal;
        }

        /**
         * Gets the Azure DNS Zone name where to replicate the non-Azure DNS Zone.
         * @return the Azure DNS Zone.
         */
        public String getAzureZoneName() {
            return this.azureZoneName;
        }

        /**
         * Gets the Azure Resource Group in which the Azure DNS Zone stands.
         * @return the Azure Resource Group to which the DNS Zone belongs.
         */
        public String getAzureResourceGroup() {
            return this.azureResourceGroup;
        }

        /**
         * Gets the Azure Service Principal with which the Azure API must authenticate.
         * @return the Azure Service Principal which the Azure API must use to authenticate.
         */
        public String getAzureServicePrincipal() {
            return this.azureServicePrincipal;
        }

        /**
         * Gets the Azure Subscription in which the Resource Group stands.
         * @return the Azure Subscription to which the Resource Group belongs.
         */
        public String getAzureSubscription() {
            return this.azureSubscription;
        }
    }

    /**
     * The types of Zone Transfers: IXFR, AXFR.
     */
    public enum ZoneTransferType {
        /**
         * Incremental Transfer Type (RFC1995).
         */
        IXFR,
        /**
         * Full Transfer Type (RFC5936).
         * */
        AXFR
    }

    /**
     * Azure Credentials Configuration.
     */
    public final class AzureCredentials {
        private String tenant;
        private String servicePrincipal;
        private String password;

        /**
         * Creates an AzureCredentials object.
         * @param tenant The tenantID to which the {@link #servicePrincipal} belongs.
         * @param servicePrincipal The {@link #servicePrincipal} to be used for authentication.
         * @param password The password to authenticate with the #servicePrincipal.
         */
        AzureCredentials(String tenant, String servicePrincipal, String password)
            throws DNSServerConfigurationException {

            this.tenant = tenant;
            setServicePrincipal(servicePrincipal);
            this.password = password;
        }

        private void setServicePrincipal(String newServicePrincipal) throws DNSServerConfigurationException {
            try {
                UUID.fromString(newServicePrincipal);
            } catch (IllegalArgumentException iae) {
                logger.error(ConfigurationText.CONF_AZURECREDENTIALS_GUIDERROR, servicePrincipal);
                throw new DNSServerConfigurationException(
                    i18n.getMessage(ConfigurationText.CONF_AZURECREDENTIALS_GUIDERROR, servicePrincipal)
                );
            }
            this.servicePrincipal = newServicePrincipal;
        }

        /**
         * Gets azure Tenant ID for the {@link AzureCredentials}.
         * @return The Azure Tenant ID.
         */
        public String getTenant() {
            return this.tenant;
        }

        /**
         * Gets the Azure Service Principal identifier.
         * @return the Azure Service Principal identifier.
         */
        public String getServicePrincipal() {
            return this.servicePrincipal;
        }

        /**
         * Gets the Azure Service Principal's password in Plain-text.
         * @return the Azure Service Principal password in Plain-text.
         */
        public String getPassword() {
            return this.password;
        }
    }

}
