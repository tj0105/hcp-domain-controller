package org.onosproject.system.domain;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.ExternalResourceReleasable;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.system.HCPMessageDecoder;
import org.onosproject.system.HCPMessageEncoder;

/**
 * @Author ldy
 * @Date: 20-2-29 下午12:17
 * @Version 1.0
 */
public class HCPDomainPipeLineFactory implements ChannelPipelineFactory, ExternalResourceReleasable {
    private HCPDomainController domainController;
    protected Timer timer;
    protected IdleStateHandler idleStateHandler;
    protected ReadTimeoutHandler readTimeoutHandler;


    public HCPDomainPipeLineFactory(HCPDomainController domainController) {
        super();
        this.domainController = domainController;
        this.timer = new HashedWheelTimer();
        this.idleStateHandler = new IdleStateHandler(timer, 20, 25, 0);
        this.readTimeoutHandler = new ReadTimeoutHandler(timer, 30);


    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        HCPDomainChannelHandler handler = new HCPDomainChannelHandler(domainController);

        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("hcpmessageDecoder", new HCPMessageDecoder());
        pipeline.addLast("hcpmessageEncoder", new HCPMessageEncoder());
        pipeline.addLast("idle", idleStateHandler);
        pipeline.addLast("timeout", readTimeoutHandler);
        pipeline.addLast("handler", handler);
        return pipeline;
    }

    @Override
    public void releaseExternalResources() {
        timer.stop();
    }
}
