package com.objectcode.lostsocks.client.net;

import org.jboss.netty.buffer.ChannelBuffer;

public interface ISocksProtocolCallback {
    void sendResponse(ChannelBuffer response);

    boolean connect(String hostOrIP, int port);
}
