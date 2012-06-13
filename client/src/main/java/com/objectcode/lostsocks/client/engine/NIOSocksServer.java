package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.net.ISocksProtocolCallback;
import com.objectcode.lostsocks.client.net.SocksProtocol;
import com.objectcode.lostsocks.client.net.SocksProtocolFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOSocksServer extends NIOServerBase {
    private static final Logger log = LoggerFactory.getLogger(NIOSocksServer.class);

    public NIOSocksServer(IConfiguration configuration) {
        super(configuration, configuration.getSocksPort());

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new SocksServerHandler());
            }
        });
    }

    private class SocksServerHandler extends ServerHandlerBase {
        SocksProtocol socksProtocol = null;

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            channel = e.getChannel();
            super.channelConnected(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
            ChannelBuffer msg = (ChannelBuffer) e.getMessage();

            channel = e.getChannel();

            if ( upChannel != null ) {
                upChannel.write(msg);
                return;
            }

            if (socksProtocol == null)
                socksProtocol = SocksProtocolFactory.create(msg);

            if (connectionId == null) {
                socksProtocol.processMessage(msg, new ISocksProtocolCallback() {
                    public void sendResponse(ChannelBuffer response) {
                        e.getChannel().write(response);
                    }

                    public void connect(String hostOrIP, int port, final IRequestCallback callback) {
                        if (isLocalNetwork(hostOrIP)) {
                            log.info("Destination: " + hostOrIP + " is local");
                            connectLocal(hostOrIP, port, callback);
                        } else {
                            sendConnectionRequest(hostOrIP + ":" + port, new IRequestCallback() {
                                public void onSuccess(CompressedPacket result) {
                                    callback.onSuccess(result);

                                    startDownPoll();
                                }

                                public void onFailure(int statusCode, String statusText) {
                                    callback.onFailure(statusCode, statusText);
                                }
                            });
                        }
                    }
                });
            } else {
                sendRequest(msg, null);
            }
        }
    }
}
