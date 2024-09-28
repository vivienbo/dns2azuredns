# dnsaxfr2azuredns
`dnsaxfr2azuredns` is a reponse to [Azure DNS](https://learn.microsoft.com/en-us/azure/dns/dns-overview) lack of [AXFR/IXFR](https://learn.microsoft.com/en-us/azure/dns/dns-overview) Zone transfer. This is a Java application that acts as a Slave DNS Server and synchronizes with Master DNS's, and then replicates modified data with Azure DNS.

This project is still in development and not yet fit for production nor fully documented.

# How to use this project

## Solution Design

Here is an example of workflow synchronizing one or more Master DNS Servers with Azure DNS.

```mermaid
sequenceDiagram
    participant EDNS as Existing DNS Server
    participant D2AD as dnsaxfr2azuredns
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

## Running the DNSSlaveServerApp

`java -jar dnsaxfr2azuredns -`

# Contributing details

## Java version and Build tools

* Building the JAR application requires Maven 2.7+.
* Code is designed for Java SE 21.

## Project Dependencies

This project requires the following libraries:
* [dnsjava](https://github.com/dnsjava/dnsjava)
* [Azure SDK for Java](https://github.com/Azure/azure-sdk-for-java)

For `DNSSlaveServerApp` command line arguments:
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)

For JSON configuration processing:
* [json-schema-validator](https://github.com/networknt/json-schema-validator)
* [Google GSON](https://github.com/google/gson)