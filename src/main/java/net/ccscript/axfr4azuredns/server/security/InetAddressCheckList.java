package net.ccscript.axfr4azuredns.server.security;

import java.net.InetAddress;

/**
 * Interface used to implement White &amp; Black lists for InetAddresses.
 */
public interface InetAddressCheckList {

    /**
     * Checks if the {@link InetAddress} is allowed or not in the list.
     * @param inetAddress The {@link InetAddress} to be tested.
     * @return {@code true} if and only if the address is allowed.
     */
    boolean isAllowed(InetAddress inetAddress);

    /**
     * Checks if the {@link String} representing an {@link InetAddress} is allowed or not in the list.
     * @param inetAddress The {@link String} representing the address to be tested.
     * @return {@code true} if and only if the address is allowed.
     */
    boolean isAllowed(String ipAddress);

}
