# Configuration File Format

## Servers Section

The `servers` section defines an array of DNS server configurations. Each server configuration is represented as an object containing the following properties:

### Properties

- **listen_on** (string, required):  
  The IP address or hostname on which the server listens for incoming DNS requests. This field is mandatory.

- **tcp_port** (number, optional):  
  The TCP port on which the server listens for incoming connections. The default value is `53`. It must be within the range of `1` to `65535`.

- **udp_port** (number, optional):  
  The UDP port on which the server listens for incoming DNS requests. It must be within the range of `1` to `65535`. If absent, UDP will be disabled.

## Zones Section

The `zones` section defines an array of DNS zone configurations. Each zone configuration is represented as an object containing the following properties:

### Properties

- **zone_name** (string, required):  
  The name of the DNS zone. This field is mandatory.

- **dns** (object, required):  
  Contains the DNS-related configuration for the zone, which includes:

  - **masters** (array of strings, required):  
    A list of master DNS servers for the zone. This field must contain at least one entry.

  - **zone_transfer** (string, optional):  
    Specifies the type of zone transfer. It can be either `"ixfr"` (incremental zone transfer) or `"axfr"` (full zone transfer). The default value is `"ixfr"`.

  - **polling_interval** (number, optional):  
    The interval in minutes for polling the master servers. It must be between `1` and `60` minutes, with a default value of `5`.

- **azure** (object, required):  
  Contains Azure-specific configuration for the zone, which includes:

  - **zone_name** (string, required):  
    The name of the Azure DNS zone.

  - **resourcegroup** (string, required):  
    The name of the Azure resource group where the DNS zone is located.

  - **subscription** (string, required):  
    The Azure subscription ID associated with the DNS zone.

  - **service_principal** (string, required):  
    The service principal identifier used for authentication with Azure.

## Azure Credentials Section

The `azure_credentials` section defines an array of Azure credentials required for authentication. Each credential configuration is represented as an object containing the following properties:

### Properties

- **tenant** (string, required):  
  The Azure Active Directory (AAD) tenant ID associated with the subscription. This field is mandatory.

- **service_principal** (string, required):  
  The service principal identifier used for authentication. This field is mandatory.

- **password** (string, required):  
  The password or secret associated with the service principal. This field is mandatory.

## Example Configuration for DNS Zone "contoso.com"

This section provides a complete configuration example for a DNS zone named **contoso.com**, including the `servers`, `zones`, and `azure_credentials` sections.

```json
{
  "servers": [
    {
      "listen_on": "0.0.0.0",
      "tcp_port": 53,
      "udp_port": 53
    },
    {
      "listen_on": "192.168.1.1",
      "tcp_port": 853
    }
  ],
  "zones": [
    {
      "zone_name": "contoso.com",
      "dns": {
        "masters": ["192.0.2.1"],
        "zone_transfer": "axfr",
        "polling_interval": 10
      },
      "azure": {
        "zone_name": "contoso-com",
        "resourcegroup": "MyResourceGroup",
        "subscription": "12345678-1234-1234-1234-123456789abc",
        "service_principal": "sp-12345678"
      }
    }
  ],
  "azure_credentials": [
    {
      "tenant": "12345678-1234-1234-1234-123456789abc",
      "service_principal": "sp-12345678",
      "password": "your-secret-password"
    }
  ]
}
```

### Explanation of the Example

- **Servers Section**:  
  - The first server listens on all interfaces (`0.0.0.0`) using the default DNS ports (TCP and UDP 53).
  - The second server listens on `192.168.1.1` using TCP port 853 and has UDP disabled.

- **Zones Section**:  
  - The zone `contoso.com` is configured with:
    - A master server at `192.0.2.1`.
    - Zone transfer set to `axfr` (full zone transfer).
    - Polling interval set to `10` minutes.
  - The Azure configuration includes:
    - The Azure DNS zone name as `contoso-com`.
    - The resource group named `MyResourceGroup`.
    - The subscription ID and service principal needed for Azure authentication.

- **Azure Credentials Section**:  
  - Contains the required credentials for Azure authentication, including the tenant ID, service principal, and password.

This example provides a complete configuration for a DNS setup using the **contoso.com** zone. Feel free to modify any values to fit your specific use case!

