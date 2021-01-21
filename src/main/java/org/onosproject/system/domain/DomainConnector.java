package org.onosproject.system.domain;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.hcp.protocol.*;
import org.onosproject.hcp.types.DomainId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.groupedThreads;

/**
 * @Author ldy
 * @Date: 20-2-29 上午11:57
 * @Version 1.0
 */
public class DomainConnector {
    public static final Logger log = LoggerFactory.getLogger(DomainConnector.class);

    private static final HCPFactory FACTORY = HCPFactories.getFactory(HCPVersion.HCP_10);

    private ChannelGroup channelGroup;

    protected int workerThreads = 16;

    private NioClientSocketChannelFactory execFactory;
    private ClientBootstrap bootstrap;
    private long systemStartTime;

    private HCPDomainController domainController;

    private final static int SEND_BUUFER_SIZE = 4 * 1024 * 1024;

    public DomainConnector(HCPDomainController domainController) {
        this.domainController = domainController;
    }

    public HCPFactory getHCPMessageFactory() {
        return FACTORY;
    }

    public long getSystemStartTime() {
        return systemStartTime;
    }

    public void run() {
        log.info("domainController superip:{} superPort:{}", domainController.getHCPSuperIp(), domainController.getHCPSuperPort());
        try {
            bootstrap = createBootStrap();
            bootstrap.setOption("reuseAddr", true);
            bootstrap.setOption("child.KeepAlive", true);
            bootstrap.setOption("child.tcpNodelay", true);
            bootstrap.setOption("child.senBufferSize", SEND_BUUFER_SIZE);

            ChannelPipelineFactory channelPipelineFactory = new HCPDomainPipeLineFactory(domainController);
            bootstrap.setPipelineFactory(channelPipelineFactory);
            channelGroup = new DefaultChannelGroup();
            InetSocketAddress sa = new InetSocketAddress(domainController.getHCPSuperIp(), domainController.getHCPSuperPort());
            bootstrap.connect(sa);
        } catch (Exception e) {
            throw new RuntimeException("");
        }
    }


    private ClientBootstrap createBootStrap() {
        if (workerThreads == 0) {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/hcpdomain", "boss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/hcpdomain", "worker-%d")));
            return new ClientBootstrap(execFactory);
        } else {
            execFactory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(groupedThreads("onos/hcpdomain", "boss-%d")),
                    Executors.newCachedThreadPool(groupedThreads("onos/hcpdomain", "worker-%d")),
                    workerThreads);
            return new ClientBootstrap(execFactory);
        }
    }

    public ClientBootstrap getBootstrap() {
        return bootstrap;
    }

    public void init() {
        this.systemStartTime = System.currentTimeMillis();
    }


    public void start() {
        log.info("Started");
        this.init();
        this.run();
    }

    public void stop() {
        log.info("Stopped");
        execFactory.shutdown();
        channelGroup.close();
    }

    public Set<HCPConfigFlags> getFlags() {
        return domainController.getFlags();
    }

    public void setFlags(Set<HCPConfigFlags> flags) {
        domainController.setFlags(flags);

    }

    public int getPeriod() {
        return domainController.getPeriod();
    }

    public void setPeriod(int period) {
        domainController.setPeriod(period);
    }

    public long getMissSendLen() {
        return domainController.getMissSendLength();
    }

    public void setMissSendLen(long missSendLen) {
        domainController.setMissSendLength(missSendLen);
    }

    public Set<HCPCapabilities> getCapabilities() {
        return domainController.getCapabilities();
    }

    public DomainId getDomainId() {
        return domainController.getDomainId();
    }

    public HCPSbpType getHCPSbpType() {
        return domainController.getHCPSbpType();
    }

    public HCPSbpVersion getHCPSbpVersion() {
        return domainController.getSbpVersion();
    }
}
