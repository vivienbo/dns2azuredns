package net.ccscript.axfr4azuredns.server.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Contains a Set of {@link DNSAccessRight} and allows to poll for DNS rights.
 */
public class DNSAccessRightSet {

    private Set<DNSAccessRight> accessRights = Collections.newSetFromMap(
        new HashMap<DNSAccessRight, Boolean>()
    );

    /**
     * Creates a new, empty {@link DNSAccessRightSet}.
     */
    public DNSAccessRightSet() {
    }

    /**
     * Gets a copy of the {@link DNSAccessRight}s applied.
     * @return A {@link Collection} of {@link DNSAccessRight}.
     */
    Collection<DNSAccessRight> getRights() {
        return Collections.unmodifiableSet(accessRights);
    }

    /**
     * Adds a right to the {@link DNSAccessRightSet}.
     * @param accessRight The right to be added to the {@link DNSAccessRightSet}.
     */
    public void addRight(DNSAccessRight accessRight) {
        accessRights.add(accessRight);
    }

    /**
     * Adds multiple access rights to the {@link DNSAccessRightSet}.
     * @param addedAccessRights The rights to be added to the {@link DNSAccessRightSet}.
     */
    public void addRights(DNSAccessRight... addedAccessRights) {
        for (DNSAccessRight accessRight : addedAccessRights) {
            this.accessRights.add(accessRight);
        }
    }

    /**
     * Adds multiple rights to the {@link DNSAccessRightSet}.
     * @param addedAccessRights The {@link Collection} of rights to be added to the {@link DNSAccessRightSet}.
     */
    public void addRights(Collection<DNSAccessRight> addedAccessRights) {
        for (DNSAccessRight right : addedAccessRights) {
            accessRights.add(right);
        }
    }

    /**
     * Adds multiple rights to the {@link DNSAccessRightSet}.
     * @param accessRightsSet The {@link DNSAccessRightSet} to be merged into the {@link DNSAccessRightSet}.
     */
    public void addRights(DNSAccessRightSet accessRightsSet) {
        this.addRights(accessRightsSet.getRights());
    }

    /**
     * Removes a right from the {@link DNSAccessRightSet}.
     * @param accessRight The right to be removed from the {@link DNSAccessRightSet}.
     */
    public void removeRight(DNSAccessRight accessRight) {
        accessRights.remove(accessRight);
    }

    /**
     * Whether the right to query a domain name is granted or not.
     * @return {@code true} if and only if the domain name request is allowed.
     */
    public boolean canQueryDomainName() {
        return !accessRights.contains(DNSAccessRight.DENY)
            && accessRights.contains(DNSAccessRight.QUERY_DN);
    }

    /**
     * Whether the right to transfer a domain name is granted or not.
     * @return {@code true} if and only if the domain name transfer is allowed.
     */
    public boolean canQueryDomainNameTransfer() {
        return !accessRights.contains(DNSAccessRight.DENY)
            && accessRights.contains(DNSAccessRight.QUERY_DN_TRANSFER);
    }

    /**
     * Combines multiple sets to calculate actual access right.
     * @param allSets All the sets to be combined.
     * @return A {@link DNSAccessRightSet} that represents the combination.
     */
    public static DNSAccessRightSet combineDnsAccessRightSet(DNSAccessRightSet... allSets) {
        DNSAccessRightSet resultingSet = new DNSAccessRightSet();
        for (DNSAccessRightSet thisSet : allSets) {
            resultingSet.addRights(thisSet.getRights());
        }
        return resultingSet;
    }

}
