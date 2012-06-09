package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.Tunnel;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NIOGenericServer extends NIOServerBase {
    private static final Logger log = LoggerFactory.getLogger(NIOGenericServer.class);

    private final Tunnel tunnel;

    public NIOGenericServer(Tunnel tunnel, IConfiguration configuration) {
        super(configuration, tunnel.getLocalPort());
        this.tunnel = tunnel;

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                return Channels.pipeline(new GenericServerHandler());
            }
        });
    }

    private class GenericServerHandler extends ServerHandlerBase {
        public GenericServerHandler() {
        }

        @Override
        public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
            String destinationUri = tunnel.getDestinationUri();

            sendConnectionRequest(destinationUri, new IRequestCallback() {
                public void onSuccess(CompressedPacket result) {
                    channel = e.getChannel();
                    connectDownStream(connectionId, new IDownStreamCallback() {
                        public void sendData(byte[] data) {
                            ChannelBuffer buffer = HeapChannelBufferFactory.getInstance().getBuffer(data, 0, data.length);
                            if (channel.isWritable())
                            channel.write(buffer);
                        }

                        public void sendEOF() {
                            if (channel.isWritable())
                                channel.close();
                        }
                    });
                }

                public void onFailure(int statusCode, String statusText) {
                    e.getChannel().close();
                }
            });
            super.channelConnected(ctx, e);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            ChannelBuffer data = (ChannelBuffer) e.getMessage();

            if (channel != null) {
                sendRequest(data, new IRequestCallback() {
                    public void onSuccess(CompressedPacket result) {
                    }

                    public void onFailure(int statusCode, String statusText) {
                    }
                });
            } else
                log.error("Dont know anything about  " + e.getChannel());
        }

    }

}
