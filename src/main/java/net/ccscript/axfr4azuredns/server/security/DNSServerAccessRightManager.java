package net.ccscript.axfr4azuredns.server.security;

import java.net.InetAddress;

/**
 * Manages the Access Rights.
 */
public interface DNSServerAccessRightManager {

    /**
     * Checks if the IP address is allowed to connect. Otherwise, decline connection.
     * @param ipAddress The IP Address of the remote host.
     * @return {@code true} if the IP Address is allowed to connect, false otherwise.
     */
    boolean isAllowed(InetAddress ipAddress);

    /**
     * Checks what {@link DNSAccessRightSet} an IP Addresss benefits from.
     * @param ipAddress The IP Address of the remote host.
     * @param domainName The domain name that is being queried.
     * @return The {@link DNSAccessRightSet} that applies to the queried {@code domainName}.
     */
    DNSAccessRightSet getAccessRightSet(InetAddress ipAddress, String domainName);

}
