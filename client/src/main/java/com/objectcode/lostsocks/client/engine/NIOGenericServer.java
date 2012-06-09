package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.Tunnel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.*;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class NIOGenericServer {
    private static final Log log = LogFactory.getLog(NIOGenericServer.class);

    private final ServerBootstrap bootstrap;

    private final IConfiguration configuration;
    private final Tunnel tunnel;

    private Channel binding;

    public NIOGenericServer(Tunnel tunnel, IConfiguration configuration) {

        this.configuration = configuration;
        this.tunnel = tunnel;

        bootstrap = NIOBackend.INSTANCE.createServer();

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new GenericServerHandler());
            }
        });
    }

    public void start() {
        if (configuration.isListenOnlyLocalhost()) {
            try {
                binding = bootstrap.bind(new InetSocketAddress(InetAddress.getLocalHost(), tunnel.getLocalPort()));
            } catch (UnknownHostException e) {
                log.error("Failed to bind to localhost", e);
            }
        }
        if (binding == null) {
            binding = bootstrap.bind(new InetSocketAddress(tunnel.getLocalPort()));
        }
    }

    public void stop() {
        binding.unbind();
        binding = null;
    }

    private class GenericServerHandler extends SimpleChannelUpstreamHandler {
        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            String destinationUri = tunnel.getDestinationUri();

            log.info("<CLIENT> An application asked a connection to " + destinationUri);

            CompressedPacket connectionCreate = new CompressedPacket(destinationUri + ":" + configuration.getTimeout(), false);
            try {
                log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
                CompressedPacket connectionCreateResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.CONNECTION_CREATE, null, connectionCreate);
                if (connectionCreateResult != null) {
                    String data[] = connectionCreateResult.getDataAsString().split(":");
                    String connectionId = data[0];
                    log.info("<SERVER> Connection created : " + connectionId);
                    ProxyInfo proxyInfo = new ProxyInfo(connectionId, e.getChannel());
                    e.getChannel().setAttachment(proxyInfo);
                    proxyInfo.schedulePoll();
                } else {
                    log.error("<SERVER> Connection creation failed");
                }
            } catch (Exception ex) {
                log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + ex, ex);
                return;
            }
            super.channelConnected(ctx, e);
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            ProxyInfo proxyInfo = (ProxyInfo) e.getChannel().getAttachment();

            if (proxyInfo != null) {
                proxyInfo.sendClose();
            } else
                log.error("Dont know anything about  " + e.getChannel());
            super.channelDisconnected(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            ChannelBuffer data = (ChannelBuffer) e.getMessage();
            ProxyInfo proxyInfo = (ProxyInfo) e.getChannel().getAttachment();

            if (proxyInfo != null) {
                proxyInfo.cancelPoll();
                proxyInfo.sendRequest(data);
                proxyInfo.schedulePoll();
            } else
                log.error("Dont know anything about  " + e.getChannel());
        }
    }

    private class ProxyInfo implements TimerTask {
        private final Channel channel;
        private final String connectionId;
        private Timeout timeout;

        private ProxyInfo(String connectionId, Channel channel) {
            this.connectionId = connectionId;
            this.channel = channel;
        }

        void schedulePoll() {
            if (timeout != null)
                timeout.cancel();
            timeout = NIOBackend.INSTANCE.getTimer().newTimeout(this, configuration.getDelay(), TimeUnit.MILLISECONDS);
        }

        void cancelPoll() {
            if (timeout != null)
                timeout.cancel();
        }

        public void run(Timeout timeout) throws Exception {
            sendRequest(null);
            schedulePoll();
        }

        synchronized void sendRequest(ChannelBuffer data) {
            CompressedPacket connectionRequset = new CompressedPacket(data != null ? data.array() : new byte[0], false);
            CompressedPacket connectionResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.CONNECTION_REQUEST, connectionId, connectionRequset);

            if (connectionResult == null) {
                log.error("<CLIENT> Server in error,  disconnecting application");
                channel.close();
                cancelPoll();
            } else {

                byte[] resultData = connectionResult.getData();
                ChannelBuffer buffer = HeapChannelBufferFactory.getInstance().getBuffer(resultData, 0, resultData.length);
                channel.write(buffer);

                if (connectionResult.isEndOfCommunication()) {
                    log.info("<SERVER> Remote server closed the connection : " + connectionId);

                    log.info("<CLIENT> Disconnecting application (regular)");
                    channel.close();
                    cancelPoll();
                }
            }
        }

        synchronized void sendClose() {
            CompressedPacket connectionCloseResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.CONNECTION_CLOSE, connectionId, null);

            if (connectionCloseResult == null) {
                log.error("<CLIENT> SERVER fail closing");
            }
            log.info("<CLIENT> Disconnecting application (regular)");
            cancelPoll();
        }
    }

}
