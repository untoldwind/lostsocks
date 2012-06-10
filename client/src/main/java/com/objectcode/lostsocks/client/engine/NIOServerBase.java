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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
            Future<CompressedPacket> future = sendHttpMessage(RequestType.VERSION_CHECK, null, versionCheck, null);

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

    public Future<CompressedPacket> sendHttpMessage(RequestType requestType,
                                                    String connectionId,
                                                    CompressedPacket input,
                                                    final IRequestCallback callback) {
        AsyncHttpClient client = configuration.createHttpClient();

        RequestBuilder requestBuilder = requestType.getHttpRequest(configuration.getUrlString(), connectionId, input != null ? input.toBody() : null);
        requestBuilder.setRealm(configuration.getRealm());
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
                    log.error("<CLIENT> Failed request " + request.getMethod() + " " + request.getUrl() + " " + response.getStatusCode() + " " + response.getStatusText());
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("Exception " + e, e);
        }
        return null;
    }

    protected class UpRequest {
        final RequestType requestType;
        final String connectionId;
        final CompressedPacket input;
        final IRequestCallback callback;

        public UpRequest(RequestType requestType, String connectionId, CompressedPacket input, IRequestCallback callback) {
            this.requestType = requestType;
            this.connectionId = connectionId;
            this.input = input;
            this.callback = callback;
        }
    }

    protected class ServerHandlerBase extends SimpleChannelUpstreamHandler {
        protected String connectionId;
        protected Channel channel;
        protected Queue<UpRequest> upRequests = new ConcurrentLinkedQueue<UpRequest>();
        protected AtomicBoolean upOpen = new AtomicBoolean();
        protected AtomicBoolean downOpen = new AtomicBoolean();

        protected IRequestCallback downStreamCallback = new IRequestCallback() {
            public void onSuccess(CompressedPacket result) {
                downOpen.set(false);
                byte[] data = result.getData();
                ChannelBuffer buffer = HeapChannelBufferFactory.getInstance().getBuffer(data, 0, data.length);
                if (channel.isWritable())
                    channel.write(buffer);
                if (!result.isEndOfCommunication()) {
                    log.info("Get package, reconnect");
                    startDownPoll();
                } else if (channel.isWritable()) {
                    downOpen.set(false);
                    channel.close();
                }
            }

            public void onFailure(int statusCode, String statusText) {
            }
        };

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            if (channel != null && connectionId != null) {
                sendClose(null);
            } else
                log.error("Dont know anything about  " + e.getChannel());
            super.channelDisconnected(ctx, e);    //To change body of overridden methods use File | Settings | File Templates.
        }

        protected void startDownPoll() {
            if (downOpen.getAndSet(true))
                return;
            sendHttpMessage(RequestType.CONNECTION_GET, connectionId, null, downStreamCallback);
        }

        protected void startUpPush() {
            if (upOpen.getAndSet(true))
                return;
            final UpRequest request = upRequests.poll();
            if (request == null) {
                upOpen.set(false);
                return;
            }
            sendHttpMessage(request.requestType, request.connectionId, request.input, new IRequestCallback() {
                public void onSuccess(CompressedPacket result) {
                    upOpen.set(false);
                    if (request.callback != null)
                        request.callback.onSuccess(result);
                    startUpPush();
                }

                public void onFailure(int statusCode, String statusText) {
                    upOpen.set(false);
                    if (request.callback != null)
                        request.callback.onFailure(statusCode, statusText);
                }
            });
        }

        protected void sendConnectionRequest(String destinationUri, final IRequestCallback callback) {
            log.info("<CLIENT> An application asked a connection to " + destinationUri);

            CompressedPacket connectionCreate = new CompressedPacket(destinationUri + ":" + configuration.getTimeout() + ":stream", false);
            try {
                log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
                upRequests.offer(new UpRequest(RequestType.CONNECTION_CREATE, null, connectionCreate,
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
                        }));
                startUpPush();
            } catch (Exception ex) {
                log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + ex, ex);
                callback.onFailure(600, ex.toString());
            }
        }

        protected void sendRequest(ChannelBuffer data, final IRequestCallback callback) {
            CompressedPacket connectionRequset = new CompressedPacket(data != null ? data.array() : new byte[0], false);
            upRequests.offer(new UpRequest(RequestType.CONNECTION_REQUEST, connectionId, connectionRequset,
                    new IRequestCallback() {
                        public void onSuccess(CompressedPacket result) {
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
                    }));
            startUpPush();
        }

        protected void sendClose(final IRequestCallback callback) {
            upRequests.offer(new UpRequest(RequestType.CONNECTION_CLOSE, connectionId, null,
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
                    }));
            startUpPush();
        }
    }
}
