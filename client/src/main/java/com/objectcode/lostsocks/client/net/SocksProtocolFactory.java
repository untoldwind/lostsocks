package com.objectcode.lostsocks.client.net;

import org.jboss.netty.buffer.ChannelBuffer;

public class SocksProtocolFactory {
    public static SocksProtocol create(ChannelBuffer msg) {
        if (msg.capacity() >= 1) {
            if (msg.getByte(0) == 4) {
                return new Socks4Protocol();
            } else if (msg.getByte(0) == 5) {
                return new Socks5Protocol();
            }
        }
        throw new RuntimeException("unknown protocol");
    }
}
