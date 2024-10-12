package net.ccscript.axfr4azuredns.server.configuration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.AzureDomain;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.DNSDomain;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.Server;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.Zone;
import net.ccscript.axfr4azuredns.server.configuration.DNSServerConfiguration.ZoneTransferType;

/**
 * Deserializes a {@link DNSServerConfiguration} from JSON using GSON library.
 */
public class DNSServerConfigurationDeserializer implements
    JsonDeserializer<DNSServerConfiguration> {

    @Override
    public DNSServerConfiguration deserialize(JsonElement jsonElement, Type type,
        JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        DNSServerConfiguration dnsConfiguration = new DNSServerConfiguration();

        try {
            deserializeServers(jsonObject, dnsConfiguration);
            deserializeAzureCredentials(jsonObject, dnsConfiguration);
            deserializeZones(jsonObject, dnsConfiguration);
        } catch (DNSServerConfigurationException dsce) {
            throw new JsonParseException(dsce);
        }

        return dnsConfiguration;
    }

    /**
     * Deserializes the "servers" section from the jsonConfiguration and adds them to the dnsConfiguration.
     * @param jsonConfiguration the root JSON configuration object
     * @param dnsConfiguration the {@link DNSServerConfiguration} to be populated
     * @throws JsonParseException If the JSON is not in correct format
     * @throws DNSServerConfigurationException If one of the values is not valid configuration type.
     */
    private void deserializeServers(JsonObject jsonConfiguration, DNSServerConfiguration dnsConfiguration)
        throws JsonParseException, DNSServerConfigurationException {

        JsonArray serversJsonArray = jsonConfiguration.get("servers").getAsJsonArray();

        for (JsonElement serverElement : serversJsonArray) {
            DNSServerConfiguration.Server serverConfiguration = null;
            JsonObject serverObject = serverElement.getAsJsonObject();

            String listenOn = serverObject.get("listen_on").getAsString();

            if (serverObject.has("tcp_port")) {
                int tcpPort = serverObject.get("tcp_port").getAsInt();

                if (serverObject.has("udp_port")) {
                    int udpPort = serverObject.get("udp_port").getAsInt();

                    serverConfiguration = dnsConfiguration.new Server(listenOn, tcpPort, udpPort);
                } else {
                    serverConfiguration = dnsConfiguration.new Server(listenOn, tcpPort, Server.DEFAULT_UDP_PORT);
                }
            } else {
                serverConfiguration = dnsConfiguration.new Server(listenOn,
                    Server.DEFAULT_TCP_PORT, Server.DEFAULT_UDP_PORT);
            }

            dnsConfiguration.addServer(serverConfiguration);
        }

    }

    /**
     * Deserializes the "zones" section from the jsonConfiguration and adds them to the dnsConfiguration.
     * @param jsonConfiguration the root JSON configuration object
     * @param dnsConfiguration the {@link DNSServerConfiguration} to be populated
     */
    private void deserializeZones(JsonObject jsonConfiguration, DNSServerConfiguration dnsConfiguration)
        throws JsonParseException, DNSServerConfigurationException {

        JsonArray domainsJsonArray = jsonConfiguration.get("zones").getAsJsonArray();

        for (JsonElement domainJsonElement : domainsJsonArray) {
            JsonObject domainObject = domainJsonElement.getAsJsonObject();
            String zoneName = domainObject.get("zone_name").getAsString();

            // Load the "dns" subsection
            JsonObject dnsObject = domainObject.get("dns").getAsJsonObject();
            DNSDomain dnsDomain = deserializeDNSDomain(dnsObject, dnsConfiguration);

            // Load the "azure" subsection
            JsonObject azureDnsObject = domainObject.get("azure").getAsJsonObject();
            AzureDomain azureDomain = deserializeAzureDomain(azureDnsObject, dnsConfiguration);

            Zone domain = dnsConfiguration.new Zone(zoneName, dnsDomain, azureDomain);
            dnsConfiguration.addZone(domain);
        }
    }

    private DNSDomain deserializeDNSDomain(JsonObject dnsObject, DNSServerConfiguration dnsConfiguration)
        throws JsonParseException, DNSServerConfigurationException {

        JsonArray mastersArray = dnsObject.get("servers").getAsJsonArray();
        List<String> masters = new ArrayList<String>();

        for (JsonElement masterJsonElement : mastersArray) {
            String ipAddress = masterJsonElement.getAsString();
            masters.add(ipAddress);
        }

        ZoneTransferType transferType = DNSDomain.DEFAULT_ZONE_TRANSFER_TYPE;
        if (dnsObject.has("zone_transfer")) {
            transferType = ZoneTransferType.valueOf(dnsObject.get("zone_transfer").getAsString().toUpperCase());
        }

        int pollingInterval = DNSDomain.DEFAULT_POLLING_INTERVAL;
        if (dnsObject.has("polling_interval")) {
            pollingInterval = dnsObject.get("polling_interval").getAsInt();
        }

        return dnsConfiguration.new DNSDomain(masters, transferType, pollingInterval);
    }

    private AzureDomain deserializeAzureDomain(JsonObject azureDnsObject, DNSServerConfiguration dnsConfiguration)
        throws JsonParseException, DNSServerConfigurationException {

        String azureZoneName = azureDnsObject.get("zone_name").getAsString();
        String azureResourceGroup = azureDnsObject.get("resourcegroup").getAsString();
        String azureSubscription = azureDnsObject.get("subscription").getAsString();
        String azureServicePrincipal = azureDnsObject.get("service_principal").getAsString();

        return dnsConfiguration.new AzureDomain(azureZoneName, azureSubscription,
            azureResourceGroup, azureServicePrincipal);
    }

    /**
     * Deserializes the "azure_credentials" section from the jsonConfiguration and adds them to the dnsConfiguration.
     * @param jsonConfiguration the root JSON configuration object
     * @param dnsConfiguration the {@link DNSServerConfiguration} to be populated
     */
    private void deserializeAzureCredentials(JsonObject jsonConfiguration,
        DNSServerConfiguration dnsConfiguration) throws DNSServerConfigurationException {

        JsonArray credentialsJsonArray = jsonConfiguration.get("azure_credentials").getAsJsonArray();
        for (JsonElement credentialsElement : credentialsJsonArray) {
            DNSServerConfiguration.AzureCredentials azureCredentialsConfiguration = null;
            JsonObject credentialObject = credentialsElement.getAsJsonObject();

            String tenant = credentialObject.get("tenant").getAsString();
            String servicePrincipal = credentialObject.get("service_principal").getAsString();
            String password = credentialObject.get("password").getAsString();

            azureCredentialsConfiguration = dnsConfiguration.new AzureCredentials(tenant, servicePrincipal, password);
            dnsConfiguration.addAzureCredentials(azureCredentialsConfiguration);
        }
    }

}
