package com.objectcode.lostsocks.client.engine;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpStreamHandler extends SimpleChannelUpstreamHandler {
    private static final Logger log = LoggerFactory.getLogger(NIOServerBase.class);

    private final Channel channel;
    private final String connectionId;

    public UpStreamHandler(Channel channel, String connectionId) {
        this.channel = channel;
        this.connectionId = connectionId;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        ChannelBuffer msg = (ChannelBuffer) e.getMessage();

        if (channel.isWritable()) {
            Channels.write(channel, msg);//ChannelBuffers.wrappedBuffer(bout.toByteArray()));
        }
        else {
            e.getChannel().setReadable(false);
        }
    }

    @Override
    public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (e.getChannel().isWritable()) {
            channel.setReadable(true);
        }
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        log.info("Closing " + connectionId);
        closeOnFlush(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        e.getCause().printStackTrace();
        closeOnFlush(e.getChannel());
    }

    private static void closeOnFlush(Channel ch) {
        if (ch.isConnected()) {
            ch.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
