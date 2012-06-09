package com.objectcode.lostsocks.client.net;

import com.objectcode.lostsocks.client.engine.IRequestCallback;
import org.jboss.netty.buffer.ChannelBuffer;

public interface ISocksProtocolCallback {
    void sendResponse(ChannelBuffer response);

    void connect(String hostOrIP, int port, IRequestCallback callback);
}
