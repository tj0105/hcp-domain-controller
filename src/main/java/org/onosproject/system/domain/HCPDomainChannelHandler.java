package org.onosproject.system.domain;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.onosproject.api.HCPSuper;
import org.onosproject.api.domain.HCPDomainController;
import org.onosproject.hcp.protocol.*;
import org.onosproject.system.HCPSuper10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @Author ldy
 * @Date: 20-2-29 下午12:25
 * @Version 1.0
 */
public class HCPDomainChannelHandler extends IdleStateAwareChannelHandler {
    private static final Logger log= LoggerFactory.getLogger(HCPDomainChannelHandler.class);

    private HCPDomainController domainController;
    private DomainConnector domainConnector;
    private HCPSuper hcPsuper;
    private Channel channel;
    private volatile ChannelState state;

    private HCPVersion hcpVersion;
    private HCPFactory hcpFactory;

    private int handshakeTransactionIds=-1;

    HCPDomainChannelHandler(HCPDomainController domainController){
        this.domainController=domainController;
        this.state=ChannelState.INIT;
        this.hcpVersion=domainController.getHCPVersion();
        this.hcpFactory= HCPFactories.getFactory(hcpVersion);
    }

    public void disconnectSuper(){
        hcPsuper.disConnectSuper();
    }

    enum ChannelState {
        INIT(false),
        WAIT_HELLO(false) {
            @Override
            void processHCPHello(HCPDomainChannelHandler h, HCPHello m) throws IOException{
                if (m.getVersion().wireVersion==h.getHCPVersion().wireVersion){
                    h.hcpVersion=HCPVersion.HCP_10;
                    h.hcpFactory=HCPFactories.getFactory(h.hcpVersion);
                    log.info("DomainController  Received HCPhello message {} {}",m.getVersion(),m.getXid());
                }else{
                    log.error("Received Hello of version {} from HCPSuperController at {}, but this DomainController " +
                            "work with HCP1.0. OxpSuperController disconnected ...", m.getVersion(), h.channel.getRemoteAddress());
                    h.channel.disconnect();
                    return;
                }
                h.setState(WAIT_FEATURES_REQUEST);
            }
            @Override
            void processHCPError(HCPDomainChannelHandler h, HCPErrorMessage m) throws IOException {
                logError(h, m);
            }
        },
        WAIT_FEATURES_REQUEST(false){
            @Override
            void processHCPFeatureRequest(HCPDomainChannelHandler h, HCPFeaturesRequest m) throws IOException {
                log.info("DomainController  Received HCPFeaturesRequest message {} {}",m.getVersion(),m.getXid());
                h.sendFeaturesReply();
                h.setState(WAIT_CONFIG_REQUEST);
            }
        },
        WAIT_CONFIG_REQUEST(false){
            @Override
            void processHCPGetConfigRequest(HCPDomainChannelHandler h, HCPGetConfigRequest m) throws IOException {
                log.info("DomainController  Received HCPGetConfigRequest message {} {}",m.getVersion(),m.getXid());
                h.sendGetConfigReply();
                h.hcPsuper=new HCPSuper10(h.domainController);
                h.hcPsuper.setChannel(h.channel);
                h.hcPsuper.setConnected(true);
                h.domainController.connectToSuperController(h.hcPsuper);
                h.setState(ACTIVE);
                log.info("=================Process GetConfigRequest successful=============");
                }
        },
        ACTIVE(true){
            @Override
            void processHCPError(HCPDomainChannelHandler h, HCPErrorMessage m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processHCPTopologyRequest(HCPDomainChannelHandler h, HCPTopologyRequest m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processHCPHostRequest(HCPDomainChannelHandler h, HCPHostRequest m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processHCPSetConfig(HCPDomainChannelHandler h, HCPSetConfig m) throws IOException {
                h.dispatchMessage(m);
            }

            @Override
            void processHCPSbp(HCPDomainChannelHandler h, HCPSbp m) throws IOException {
                h.dispatchMessage(m);
            }
        };
        private final boolean handshakeComplete;
        ChannelState(boolean handshakeComplete) {
            this.handshakeComplete = handshakeComplete;
        }

        public boolean isHandshakeComplete() {
            return handshakeComplete;
        }

        protected String getSuperStateMessage(HCPDomainChannelHandler h,
                                              HCPMessage m,
                                              String details) {
            return String.format("Super State:[%s],received:[%s], details:[%s]",
                    this.toString(),
                    m.getType().toString(),
                    details);
        }
        protected void unhandledMessageReceived(HCPDomainChannelHandler h,
                                                HCPMessage m) {
            if (log.isDebugEnabled()) {
                String msg = getSuperStateMessage(h, m, "Ignoring unexpected message");
                log.debug(msg);
            }
        }
        protected void logError(HCPDomainChannelHandler h, HCPErrorMessage error) {
            log.error("Oxp msg error:{} from super in state {}",
                    error,
                    this.toString());
        }

        void processHCPMessage(HCPDomainChannelHandler h,HCPMessage m) throws IOException{
            switch (m.getType()){
                case HCP_HELLO:
                    processHCPHello(h,(HCPHello) m);
                    break;
                case HCP_ERROR:
                    processHCPError(h,(HCPErrorMessage)m);
                    break;
                case HCP_ECHO_REQUEST:
                    processHCPEchoRequest(h,(HCPEchoRequest)m);
                    break;
                case HCP_ECHO_REPLY:
                    processHCPEchoReply(h,(HCPEchoReply)m);
                    break;
                case HCP_FEATURES_REQUEST:
                    processHCPFeatureRequest(h, (HCPFeaturesRequest) m);
                    break;
                case HCP_GET_CONFIG_REQUEST:
                    processHCPGetConfigRequest(h, (HCPGetConfigRequest) m);
                    break;
                case HCP_SET_CONFIG:
                    processHCPSetConfig(h, (HCPSetConfig) m);
                    break;
                case HCP_SBP:
                    processHCPSbp(h, (HCPSbp) m);
                    break;
                case HCP_TOPO_REQUEST:
                    processHCPTopologyRequest(h, (HCPTopologyRequest) m);
                    break;
                case HCP_HOST_REQUEST:
                    processHCPHostRequest(h, (HCPHostRequest) m);
                    break;
                default:
                    unhandledMessageReceived(h, m);
            }
        }
        void processHCPHello(HCPDomainChannelHandler h, HCPHello m) throws IOException {

        }

        void processHCPEchoRequest(HCPDomainChannelHandler h, HCPEchoRequest m) throws IOException {
            HCPEchoReply reply = h.hcpFactory.buildEchoReply()
                    .setData(m.getData())
                    .setXid(m.getXid())
                    .build();
            h.channel.write(Collections.singletonList(reply));
        }

        void processHCPEchoReply(HCPDomainChannelHandler h, HCPEchoReply m) throws IOException {

        }

        void processHCPError(HCPDomainChannelHandler h, HCPErrorMessage m) throws IOException {

        }

        void processHCPFeatureRequest(HCPDomainChannelHandler h, HCPFeaturesRequest m) throws IOException {

        }

        void processHCPGetConfigRequest(HCPDomainChannelHandler h, HCPGetConfigRequest m) throws IOException {

        }

        void processHCPSetConfig(HCPDomainChannelHandler h, HCPSetConfig m) throws IOException {

        }

        void processHCPTopologyRequest(HCPDomainChannelHandler h, HCPTopologyRequest m) throws IOException {

        }

        void processHCPHostRequest(HCPDomainChannelHandler h, HCPHostRequest m) throws IOException {

        }

        void processHCPSbp(HCPDomainChannelHandler h, HCPSbp m) throws IOException {

        }
    }

    private void sendHandShakeHelloMessage() throws IOException{
        HCPMessage.Builder hello=hcpFactory.buildHello().setXid(this.handshakeTransactionIds--);
        log.info("Sending HCP_10 Hello to Super: {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(hello.build()));
    }

    private void sendFeaturesReply() throws IOException {
        HCPMessage m = hcpFactory.buildFeaturesReply()
                .setCapabilities(domainController.getCapabilities())
                .setDomainId(domainController.getDomainId())
                .setSbpType(domainController.getHCPSbpType())
                .setSbpVersion(domainController.getSbpVersion())
                .setXid(this.handshakeTransactionIds--)
                .build();
        log.info("FeaturesReply message ");
        log.info("Sending HCP_10 FeatursReply to Super: {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(m));
    }

    private void sendGetConfigReply() throws IOException {
        HCPMessage m = hcpFactory.buildGetConfigReply()
                .setFlags(domainController.getFlags())
                .setPeriod((byte) domainController.getPeriod())
                .setMissSendLength((short) domainController.getMissSendLength())
                .build();
        log.info("Sending HCP_10 SendGetConfigReply to Super: {}", channel.getRemoteAddress());
        channel.write(Collections.singletonList(m));
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel=e.getChannel();
        log.info("Connecetd to hcp super controller");
        Thread.sleep(1000);
        sendHandShakeHelloMessage();
        setState(ChannelState.WAIT_HELLO) ;

    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("HCP super controller disconnected from here");
        hcPsuper.disConnectSuper();
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        super.channelIdle(ctx,e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//        log.info("MessageEvent {}",e.getMessage());
        if (e.getMessage() instanceof List) {
            List<HCPMessage> msgList = (List<HCPMessage>) e.getMessage();

            for (HCPMessage oxpm : msgList) {
                state.processHCPMessage(this, oxpm);
            }
        } else {
            state.processHCPMessage(this, (HCPMessage) e.getMessage());
        }
    }

    public boolean isHandshakeComplete() {
        return this.state.isHandshakeComplete();
    }

    private void dispatchMessage(HCPMessage m) {
        hcPsuper.handleMessage(m);
    }

    private void setState(ChannelState state) {
        this.state = state;
    }

    public HCPVersion getHCPVersion() {
        return this.hcpVersion;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.info(e.toString());
    }
}
