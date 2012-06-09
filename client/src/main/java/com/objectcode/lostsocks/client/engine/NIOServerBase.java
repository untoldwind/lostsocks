package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.Constants;
import com.objectcode.lostsocks.client.config.IConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public abstract class NIOServerBase {
    private static final Log log = LogFactory.getLog(NIOServerBase.class);

    protected final ServerBootstrap bootstrap;

    protected final IConfiguration configuration;

    protected final int listenPort;

    protected Channel binding;


    protected NIOServerBase(IConfiguration configuration, int listenPort) {
        this.configuration = configuration;
        this.listenPort = listenPort;

        bootstrap = NIOBackend.INSTANCE.getServer();
    }

    public void start() {
        if (configuration.isListenOnlyLocalhost()) {
            try {
                binding = bootstrap.bind(new InetSocketAddress(InetAddress.getLocalHost(), listenPort));
            } catch (UnknownHostException e) {
                log.error("Failed to bind to localhost", e);
            }
        }
        if (binding == null) {
            binding = bootstrap.bind(new InetSocketAddress(listenPort));
        }
    }

    public void stop() {
        binding.unbind();
        binding = null;
    }

    public boolean checkServerVersion() {
        // Create a connection on the servlet server
        CompressedPacket versionCheck = new CompressedPacket(Constants.APPLICATION_VERSION, true);

        // Send the connection
        String id = null;
        String serverInfoMessage = null;
        try {
            log.info("<CLIENT> Version check : " + Constants.APPLICATION_VERSION + " - URL : " + configuration.getUrlString());
            CompressedPacket versionCheckResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.VERSION_CHECK, null, versionCheck);

            if (versionCheckResult != null) {
                if (!Constants.APPLICATION_VERSION.equals(versionCheckResult.getDataAsString()))
                    log.warn("<SERVER> Version supported but you should use version " + id);
                else
                    log.info("<SERVER> Version check : OK");
                return true;
            } else {
                log.fatal("<SERVER> Version not supported. Version needed : " + id);

                return false;
            }
        } catch (Exception e) {
            log.fatal("<CLIENT> Version check : Cannot check the server version. Exception : ", e);
            return (false);
        }
    }

    protected class ServerHandlerBase extends SimpleChannelUpstreamHandler implements TimerTask {
        protected String connectionId;
        protected Channel channel;

        protected Timeout timeout;

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            if (channel != null && connectionId != null) {
                sendClose();
            } else
                log.error("Dont know anything about  " + e.getChannel());
            super.channelDisconnected(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
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
            if (sendRequest(null))
                schedulePoll();
        }

        protected synchronized boolean sendConnectionRequset(String destinationUri) {
            log.info("<CLIENT> An application asked a connection to " + destinationUri);

            CompressedPacket connectionCreate = new CompressedPacket(destinationUri + ":" + configuration.getTimeout(), false);
            try {
                log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
                CompressedPacket connectionCreateResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.CONNECTION_CREATE, null, connectionCreate);
                if (connectionCreateResult != null) {
                    String data[] = connectionCreateResult.getDataAsString().split(":");
                    connectionId = data[0];
                    log.info("<SERVER> Connection created : " + connectionId);
                    return true;
                } else {
                    log.error("<SERVER> Connection creation failed");
                }
            } catch (Exception ex) {
                log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + ex, ex);
            }

            return false;
        }

        protected synchronized boolean sendRequest(ChannelBuffer data) {
            CompressedPacket connectionRequset = new CompressedPacket(data != null ? data.array() : new byte[0], false);
            CompressedPacket connectionResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.CONNECTION_REQUEST, connectionId, connectionRequset);

            if (connectionResult == null) {
                log.error("<CLIENT> Server in error,  disconnecting application");
                channel.close();
                return false;
            } else {

                byte[] resultData = connectionResult.getData();
                ChannelBuffer buffer = HeapChannelBufferFactory.getInstance().getBuffer(resultData, 0, resultData.length);
                channel.write(buffer);

                if (connectionResult.isEndOfCommunication()) {
                    log.info("<SERVER> Remote server closed the connection : " + connectionId);

                    log.info("<CLIENT> Disconnecting application (regular)");
                    channel.close();
                    return false;
                }
                return true;
            }
        }

        protected synchronized void sendClose() {
            CompressedPacket connectionCloseResult = ThreadCommunication.sendHttpMessage(configuration, RequestType.CONNECTION_CLOSE, connectionId, null);

            if (connectionCloseResult == null) {
                log.error("<CLIENT> SERVER fail closing");
            }
            log.info("<CLIENT> Disconnecting application (regular)");
            cancelPoll();
        }
    }
}
