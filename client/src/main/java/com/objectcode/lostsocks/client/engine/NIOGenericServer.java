package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.Tunnel;
import org.jboss.netty.buffer.ChannelBuffer;
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
            System.out.println(">>> new");
        }

        @Override
        public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
            channel = e.getChannel();
            String destinationUri = tunnel.getDestinationUri();

            sendConnectionRequest(destinationUri, new IRequestCallback() {
                public void onSuccess(CompressedPacket result) {
                    channel = e.getChannel();

                    startDownPoll();
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
                sendRequest(data, null);
            } else
                log.error("Dont know anything about  " + e.getChannel());
        }

    }

}
