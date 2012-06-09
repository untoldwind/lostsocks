package com.objectcode.lostsocks.client.engine;

import com.ning.http.client.*;
import com.objectcode.lostsocks.client.Constants;
import com.objectcode.lostsocks.client.config.IConfiguration;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class NIOServerBase {
    private static final Logger log = LoggerFactory.getLogger(NIOServerBase.class);

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
            Future<CompressedPacket> future = sendHttpMessage(configuration, RequestType.VERSION_CHECK, null, versionCheck, null);

            CompressedPacket versionCheckResult = future.get(10, TimeUnit.SECONDS);

            if (versionCheckResult != null) {
                if (!Constants.APPLICATION_VERSION.equals(versionCheckResult.getDataAsString()))
                    log.warn("<SERVER> Version supported but you should use version " + id);
                else
                    log.info("<SERVER> Version check : OK");
                return true;
            } else {
                log.error("<SERVER> Version not supported. Version needed : " + id);

                return false;
            }
        } catch (Exception e) {
            log.error("<CLIENT> Version check : Cannot check the server version. Exception : ", e);
            return (false);
        }
    }


    public static Future<CompressedPacket> sendHttpMessage(IConfiguration config,
                                                           RequestType requestType,
                                                           String connectionId,
                                                           CompressedPacket input,
                                                           final IRequestCallback callback) {
        AsyncHttpClient client = config.createHttpClient();

        RequestBuilder requestBuilder = requestType.getHttpRequest(config.getUrlString(), connectionId, input != null ? input.toBody() : null);
        requestBuilder.setRealm(config.getRealm());
        final Request request = requestBuilder.build();
        try {
            return client.executeRequest(request, new AsyncCompletionHandler<CompressedPacket>() {
                @Override
                public CompressedPacket onCompleted(Response response) throws Exception {
                    if (response.getStatusCode() == 200) {
                        CompressedPacket result = CompressedPacket.fromStream(response.getResponseBodyAsStream());
                        if (callback != null)
                            callback.onSuccess(result);
                        return result;
                    } else if (callback != null) {
                        callback.onFailure(response.getStatusCode(), response.getStatusText());
                    }
                    log.error("<CLIENT> Failed request " + request.getUrl() + " " + response.getStatusCode() + " " + response.getStatusText());
                    return null;
                }

            });
        } catch (Exception e) {
            log.error("Exception " + e, e);
        }
        return null;
    }

    protected class ServerHandlerBase extends SimpleChannelUpstreamHandler implements TimerTask {
        protected String connectionId;
        protected Channel channel;

        protected Timeout timeout;

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            if (channel != null && connectionId != null) {
                sendClose(null);
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
            sendRequest(null, new IRequestCallback() {
                public void onSuccess(CompressedPacket result) {
                    if (!result.isEndOfCommunication())
                        schedulePoll();
                }

                public void onFailure(int statusCode, String statusText) {
                }
            });
        }

        protected void sendConnectionRequest(String destinationUri, final IRequestCallback callback) {
            log.info("<CLIENT> An application asked a connection to " + destinationUri);

            CompressedPacket connectionCreate = new CompressedPacket(destinationUri + ":" + configuration.getTimeout(), false);
            try {
                log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
                sendHttpMessage(configuration, RequestType.CONNECTION_CREATE, null, connectionCreate,
                        new IRequestCallback() {
                            public void onSuccess(CompressedPacket result) {
                                String data[] = result.getDataAsString().split(":");
                                connectionId = data[0];
                                log.info("<SERVER> Connection created : " + connectionId);
                                if (callback != null)
                                    callback.onSuccess(result);
                            }

                            public void onFailure(int statusCode, String statusText) {
                                log.error("<SERVER> Connection creation failed");
                                if (callback != null)
                                    callback.onFailure(statusCode, statusText);
                            }
                        });
            } catch (Exception ex) {
                log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + ex, ex);
                callback.onFailure(600, ex.toString());
            }
        }

        protected void sendRequest(ChannelBuffer data, final IRequestCallback callback) {
            CompressedPacket connectionRequset = new CompressedPacket(data != null ? data.array() : new byte[0], false);
            sendHttpMessage(configuration, RequestType.CONNECTION_REQUEST, connectionId, connectionRequset,
                    new IRequestCallback() {
                        public void onSuccess(CompressedPacket result) {
                            byte[] resultData = result.getData();
                            ChannelBuffer buffer = HeapChannelBufferFactory.getInstance().getBuffer(resultData, 0, resultData.length);
                            if (channel.isWritable())
                                channel.write(buffer);

                            if (result.isEndOfCommunication()) {
                                log.info("<SERVER> Remote server closed the connection : " + connectionId);

                                log.info("<CLIENT> Disconnecting application (regular)");
                                channel.close();
                            }
                            if (callback != null)
                                callback.onSuccess(result);
                        }

                        public void onFailure(int statusCode, String statusText) {
                            log.error("<CLIENT> Server in error,  disconnecting application");
                            channel.close();
                            if (callback != null)
                                callback.onFailure(statusCode, statusText);
                        }
                    });
        }

        protected void sendClose(final IRequestCallback callback) {
            cancelPoll();
            sendHttpMessage(configuration, RequestType.CONNECTION_CLOSE, connectionId, null,
                    new IRequestCallback() {
                        public void onSuccess(CompressedPacket result) {
                            log.info("<CLIENT> Disconnecting application (regular)");
                            if (callback != null)
                                callback.onSuccess(result);
                        }

                        public void onFailure(int statusCode, String statusText) {
                            log.error("<CLIENT> SERVER fail closing");
                            if (callback != null)
                                callback.onFailure(statusCode, statusText);
                        }
                    });
        }
    }
}
