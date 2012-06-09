package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.net.ISocksProtocolCallback;
import com.objectcode.lostsocks.client.net.SocksProtocol;
import com.objectcode.lostsocks.client.net.SocksProtocolFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;

public class NIOSocksServer extends NIOServerBase {
    private static final Log log = LogFactory.getLog(NIOSocksServer.class);

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

            if (socksProtocol == null)
                socksProtocol = SocksProtocolFactory.create(msg);

            if (connectionId == null) {
                socksProtocol.processMessage(msg, new ISocksProtocolCallback() {
                    public void sendResponse(ChannelBuffer response) {
                        e.getChannel().write(response);
                    }

                    public boolean connect(String hostOrIP, int port) {
                        if (sendConnectionRequset(hostOrIP + ":" + port)) {
                            schedulePoll();
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                cancelPoll();
                if (sendRequest(msg))
                    schedulePoll();
            }
        }
    }
}
