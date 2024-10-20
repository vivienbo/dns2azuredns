package net.ccscript.axfr4azuredns.server.security;

import java.util.ArrayList;
import java.util.List;

import com.github.jgonian.ipmath.AbstractIp;
import com.github.jgonian.ipmath.AbstractIpRange;

public abstract class AbstractInetAddressCheckList<C extends AbstractIp<C, R>,
    R extends AbstractIpRange<C, R>> implements InetAddressCheckList {

    private List<R> ipList = new ArrayList<R>();

    public void addIpRange(R range) {
        ipList.add(range);
    }

    public void addIpRange(String range) {
    }

}
