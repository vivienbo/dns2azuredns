package net.ccscript.axfr4azuredns.server.security;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentDnsAccessRightTreeNode {

    private ConcurrentDnsAccessRightTreeNode parent;
    private Map<String, ConcurrentDnsAccessRightTreeNode> children =
        new ConcurrentHashMap<String, ConcurrentDnsAccessRightTreeNode>();
    private DNSAccessRightSet dnsAccessRights = new DNSAccessRightSet();
    private String domainNamePart;

    /**
     * Creates a new {@link ConcurrentDnsAccessRightTreeNode}.
     * @param parent The parent node of the {@link ConcurrentDnsAccessRightTreeNode}.
     * @param domainNamePart The domain name part this node represents.
     */
    public ConcurrentDnsAccessRightTreeNode(
        ConcurrentDnsAccessRightTreeNode parent, String domainNamePart) {
        this.parent = parent;
        this.domainNamePart = domainNamePart;
    }

    /**
     * Checks if the Node is the root.
     * @return {@code true} if the node is the root.
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Checks if the Node is a leaf.
     * @return {@code true} if the node is a leaf.
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns the node's parent, or {@code null} if it's the root.
     * @return The node's parent or {@code null} if the node is the root.
     */
    public ConcurrentDnsAccessRightTreeNode getParent() {
        return this.parent;
    }

    /**
     * Retrieves the children which {@link #domainNamePart} matches the parameter.
     * @param domainNameComponent The domain name part to look for. It should have no dots.
     * @return The corresponding child or {@code null} if no children matches the request.
     */
    public ConcurrentDnsAccessRightTreeNode getChild(String domainNameComponent) {
        return children.get(domainNameComponent);
    }

    /**
     * Returns the node's children, or {@code null} if it's a leaf.
     * @return The Collection of node's children or {@code null} if the node is a leaf.
     */
    public Collection<ConcurrentDnsAccessRightTreeNode> getChildren() {
        return this.children.values();
    }

    /**
     * Retrieves the {@link DNSAccessRightSet} associated to the node.
     * @return The {@link DNSAccessRightSet} associated to the node.
     *         The set is empty if no rights are associated.
     */
    public DNSAccessRightSet getDnsAccessRightSet() {
        return this.dnsAccessRights;
    }

    /**
     * Retrieves the {@link #domainNamePart} associated with the Node.
     * @return The {@link #domainNamePart} associated with the Node.
     */
    public String getDomainNamePart() {
        return this.domainNamePart;
    }

    /**
     * Finds the root from any {@link ConcurrentDnsAccessRightTreeNode}.
     * @return A {@link ConcurrentDnsAccessRightTreeNode} representing the Root of the tree.
     */
    public ConcurrentDnsAccessRightTreeNode getRoot() {
        ConcurrentDnsAccessRightTreeNode currentNode = this;
        while (!this.isRoot()) {
            currentNode = currentNode.parent;
        }
        return currentNode;
    }

    /**
     * Gathers applicable {@link DNSAccessRight}s from the domain name provided.
     * Only looks at children.
     * @return the {@link DNSAccessRightSet} corresponding to applicable rights.
     */
    public DNSAccessRightSet getRightsForDomainName(String domainNameComponent) {
        String[] domainNameComponents = domainNameComponent.split("[.]");
        ConcurrentDnsAccessRightTreeNode currentNode = this;
        DNSAccessRightSet resultSet = new DNSAccessRightSet();

        for (int i = domainNameComponents.length - 1; i > 0; --i) {
            currentNode = currentNode.getChild(domainNameComponents[i]);
            if (currentNode != null) {
                resultSet.addRights(currentNode.getDnsAccessRightSet());
            } else {
                break;
            }
        }

        return resultSet;
    }

}
