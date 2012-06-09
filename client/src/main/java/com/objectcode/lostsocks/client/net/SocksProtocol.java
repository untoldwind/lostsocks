package com.objectcode.lostsocks.client.net;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class SocksProtocol {
    public abstract void processMessage(ChannelBuffer msg,   ISocksProtocolCallback callback);
}
