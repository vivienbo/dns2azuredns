# axfr4azuredns
`axfr4azuredns` is a reponse to [Azure DNS](https://learn.microsoft.com/en-us/azure/dns/dns-overview) lack of support for [AXFR/IXFR](https://learn.microsoft.com/en-us/azure/dns/dns-faq#does-azure-dns-support-zone-transfers--axfr-ixfr--) Zone transfer.
This is a Java application that can act:
* As a Slave DNS Server which synchronizes with Master DNS's, and then replicates modified data with Azure DNS.
* Or as a Master DNS Server which synchronizes with Azure DNS, and then replicates modified data to a non-Azure, AXFR compatible DNS.

# Disclaimer

This project is not functional yet. Documentation currently represents the aim of the project.

Any available version for now stores passwords in cleartext in the JSON configuration, and in cleartext in memory. If this is a security concern for you, feel free to contribute to the project to add better security. Otherwise this will be addressed once functional.

# How to use this project

Intended use of this application is to allow transfer from a private DNS Zone hosted on any AXFR or IXFR compliant DNS Server (Windows Server DNS, bind, unbound...) to Azure DNS Zones.

It *might* be used to leverage Azure DNS as secondary DNS Server however in that case, DNSSEC cannot be enabled.

For now it was not design to allow 2-ways updates.

## Solution Design

Here is an example of workflow synchronizing one or more Master DNS Servers with Azure DNS.

```mermaid
sequenceDiagram
    participant EDNS as Existing DNS Server
    participant D2AD as axfr4azuredns
    participant AzDNSCache as Local Azure DNS Cache
    participant AzAPI as Azure API
    participant AzDNS as Azure DNS
    
    alt On Startup if Cache enabled
        AzDNSCache->>AzAPI: GET dnsZones/{zoneName}/all
        AzAPI->>AzDNSCache: RecordSetListResult
    end

    alt DNS NOTIFY not received
        loop Every polling_interval minutes
            D2AD->>EDNS: DNS SOA request
            EDNS->>D2AD: DNS SOA response
        end
    else DNS NOTIFY received
        EDNS->>D2AD: DNS NOTIFY
    end

    alt IXFR support and functional
        D2AD->>EDNS: IXFR request
        EDNS->>D2AD: IXFR response
    else AXFR fallback
        D2AD->>EDNS: Fallback to AXFR if IXFR fails
        EDNS->>D2AD: AXFR response
    end

    alt Compare IXFR/AXFR to Cache
        D2AD->>AzDNSCache: Compare records to cache
        AzDNSCache->>D2AD: List records requiring update / creation / deletion
    else Compare IXFR/AXFR to Azure API
        D2AD->>AzAPI: Compare records to live Azure DNS data
        AzAPI->>D2AD: List records requiring update / creation / deletion
    end

    D2AD->>AzAPI: Run update / creation / deletion requests

    par API feedback
        AzAPI->>D2AD: 200 OK
    and Azure DNS Propagation
        AzAPI->>AzDNS: Propagation of updates to Azure DNS
    end
```

## Running the DNSServerApp

1. Create a configuration file based on the [example.json](conf/example.json). For more information check [Configuration Documentation](conf/README.md)
2. Start the server using `java -jar axfr4azuredns.jar -c conf/settings.json`

# Contributing details

## Java version and Build tools

* Building the JAR application requires Maven 2.7+.
* Code is designed for Java SE 21 (may look into lower versions if functional).

## Project Dependencies

This project requires the following libraries:
* [dnsjava](https://github.com/dnsjava/dnsjava)
* [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java)

For `DNSServerApp` command line arguments:
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)

For JSON configuration processing:
* [dev.harrel's json-schema](https://github.com/harrel56/json-schema)
* [Google GSON](https://github.com/google/gson)
* [Apache Commons Validator](https://commons.apache.org/proper/commons-validator/)

# Todo List

To get to the first version, we need:
* [ ] Configuration object
* [ ] Functional DNS Server to receive NOTIFY or send AXFR requests every (configurable) minutes
* [ ] DNS Caching for IXFR transfers
* [ ] Azure DNS Zone to dnsjava converter
* [ ] DNS Caching for Azure DNS
* [ ] Comparator between the DNS Caching and Azure DNS Zone
* [ ] Updater for the Azure DNS Zone