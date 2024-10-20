package net.ccscript.axfr4azuredns.server.security;

import java.net.InetAddress;

/**
 * A basic {@link DNSServerAccessRightManager} implementation that allows any IP address,
 * and gives maximum access rights.
 */
public class AllowAllDNSServerAccessRightManager implements DNSServerAccessRightManager {

    private DNSAccessRightSet accessRightSet = new DNSAccessRightSet();

    public AllowAllDNSServerAccessRightManager() {
        accessRightSet.addRights(DNSAccessRight.QUERY_DN, DNSAccessRight.QUERY_DN_TRANSFER);
    }

    @Override
    public boolean isAllowed(InetAddress ipAddress) {
        return true;
    }

    @Override
    public DNSAccessRightSet getAccessRightSet(InetAddress ipAddress, String domainName) {
        return accessRightSet;
    }

}
