package com.objectcode.lostsocks.client.net;

import com.objectcode.lostsocks.client.engine.CompressedPacket;
import com.objectcode.lostsocks.client.engine.IRequestCallback;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;

public class Socks4Protocol extends SocksProtocol {
    static final byte VER_4 = 0x00;

    public static final byte SOCKS4_OK = 90; // request granted
    public static final byte SOCKS4_KO = 91; // request rejected or failed

    @Override
    public void processMessage(ChannelBuffer msg,  final ISocksProtocolCallback callback) {
        if (msg.capacity() >= 8 && msg.getByte(0) == 4 && msg.getByte(1) == 1) {
            final byte[] ipBytes = new byte[4];
            final byte[] portBytes = new byte[2];

            msg.getBytes(4, ipBytes);
            msg.getBytes(2, portBytes);
            int port = (((0xFF & portBytes[0]) << 8) + (0xFF & portBytes[1]));
            callback.connect(b2i(ipBytes[0]) + "." + b2i(ipBytes[1]) + "." + b2i(ipBytes[2]) + "." + b2i(ipBytes[3]), port,
                    new IRequestCallback() {
                        public void onSuccess(CompressedPacket result) {
                            callback.sendResponse(buildResponse(true, ipBytes, portBytes));
                        }

                        public void onFailure(int statusCode, String statusText) {
                            callback.sendResponse(buildResponse(false, ipBytes, portBytes));
                        }
                    });
        }
        else {
            throw new RuntimeException("invalid request type");
        }
    }

    private int b2i(byte b) {
        return (b < 0 ? 256 + b : b);
    }

    private ChannelBuffer buildResponse(boolean success, byte[] ipBytes, byte[] portBytes) {
        ChannelBuffer response = HeapChannelBufferFactory.getInstance().getBuffer(8);

        response.writeByte(VER_4);
        response.writeByte(success ? SOCKS4_OK : SOCKS4_KO);
        response.writeBytes(portBytes);
        response.writeBytes(ipBytes);

        return response;
    }
}
