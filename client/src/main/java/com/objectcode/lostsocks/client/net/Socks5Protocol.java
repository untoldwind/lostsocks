package com.objectcode.lostsocks.client.net;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.HeapChannelBufferFactory;

public class Socks5Protocol extends SocksProtocol {
    static final byte VER_5 = 0x05;
    static final byte RSV = 0x00;

    enum Command {
        CONNECT(0x01),
        BIND(0x02),
        UDP(0x03);

        private int value;

        private Command(int value) {
            this.value = value;
        }

        public byte byteValue() {
            return (new Integer(value)).byteValue();
        }
    }

    enum AddressType {
        IPV4(0x01),
        DOMAINNAME(0x03),
        IPV6(0x04);

        private int value;

        private AddressType(int value) {
            this.value = value;
        }

        public byte byteValue() {
            return (new Integer(value)).byteValue();
        }
    }

    enum ResponseCode {
        SUCCEEDED(0x00),
        GENERAL_SOCKS_SERVER_FAILURE(0x01),
        CONNECTION_NOT_ALLOWED(0x02),
        NETWORK_UNREACHABLE(0x03),
        HOST_UNREACHABLE(0x04),
        CONNECTION_REFUSED(0x05),
        TTL_EXPIRED(0x06),
        COMMAND_NOT_SUPPORTED(0x07),
        ADDRESS_TYPE_NOT_SUPPORTED(0x08);

        private int value;

        private ResponseCode(int value) {
            this.value = value;
        }

        public byte byteValue() {
            return (new Integer(value)).byteValue();
        }
    }

    enum Step {
        SELECT_METHOD,
        COMMAND
    }

    Step currentStep = Step.SELECT_METHOD;

    @Override
    public void processMessage(ChannelBuffer msg, ISocksProtocolCallback callback) {
        switch (currentStep) {
            case SELECT_METHOD:
                if (isSelectMethodRequest(msg)) {
                    ChannelBuffer response = HeapChannelBufferFactory.getInstance().getBuffer(2);
                    response.writeByte(VER_5);
                    response.writeByte(RSV);
                    callback.sendResponse(response);
                    currentStep = Step.COMMAND;
                } else
                    throw new RuntimeException("Protocol error");
                break;
            case COMMAND:
                if (isConnectionRequest(msg)) {
                    processConnectionRequest(msg, callback);
                } else if (isBindRequest(msg)) {
                    throw new RuntimeException("Bind not supported (yet?)");
                } else
                    throw new RuntimeException("Protocol error");
                break;
        }
    }

    private boolean isSelectMethodRequest(ChannelBuffer msg) {
        if (msg.capacity() >= 3 && msg.capacity() < 257) {
            int cnt = msg.getByte(1);
            if (msg.capacity() == cnt + 2) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnectionRequest(ChannelBuffer msg) {
        if (msg.capacity() < 2)
            return false;

        if (msg.getByte(0) == VER_5 && msg.getByte(1) == Command.CONNECT.byteValue()) {
            return true;
        }
        return false;
    }

    private void processConnectionRequest(ChannelBuffer msg, ISocksProtocolCallback callback) {
        if (msg.capacity() < 4)
            throw new RuntimeException("Protocol error");

        int addressType = msg.getByte(3);

        if (addressType == AddressType.IPV4.byteValue()) {
            connectIPv4(msg, callback);
        } else if (addressType == AddressType.DOMAINNAME.byteValue()) {
            connectDomain(msg, callback);
        } else if (addressType == AddressType.IPV6.byteValue()) {
            throw new RuntimeException("IPv6 not supported (yet?)");
        } else {
            throw new RuntimeException("unsupported address type " + addressType);
        }
    }

    private void connectIPv4(ChannelBuffer msg, ISocksProtocolCallback callback) {
        if (msg.capacity() < 10)
            throw new RuntimeException("Protocol error");

        byte[] ipBytes = new byte[4];
        byte[] portBytes = new byte[2];

        msg.getBytes(4, ipBytes);
        msg.getBytes(8, portBytes);

        int port = (((0xFF & msg.getByte(8)) << 8) + (0xFF & msg.getByte(9)));
        if (callback.connect(b2i(ipBytes[0]) + "." + b2i(ipBytes[1]) + "." + b2i(ipBytes[2]) + "." + b2i(ipBytes[3]), port)) {
            callback.sendResponse(buildResponse(AddressType.IPV4, ResponseCode.SUCCEEDED, ipBytes, portBytes));
        } else {
            callback.sendResponse(buildResponse(AddressType.IPV4, ResponseCode.CONNECTION_REFUSED, ipBytes, portBytes));
        }
    }

    private void connectDomain(ChannelBuffer msg, ISocksProtocolCallback callback) {
        if (msg.capacity() < 5)
            throw new RuntimeException("Protocol error");

        int cnt = msg.getByte(4);
        if (msg.capacity() < 5 + cnt + 2)
            throw new RuntimeException("Protocol error");

        byte[] domain = new byte[cnt];
        msg.getBytes(5, domain);

        byte[] portBytes = new byte[2];

        msg.getBytes(5 + cnt, portBytes);

        int port = (((0xFF & msg.getByte(5 + cnt)) << 8) + (0xFF & msg.getByte(5 + cnt + 1)));
        if (callback.connect(new String(domain), port)) {
            callback.sendResponse(buildResponse(AddressType.IPV4, ResponseCode.SUCCEEDED, new byte[4], portBytes));
        } else {
            callback.sendResponse(buildResponse(AddressType.IPV4, ResponseCode.CONNECTION_REFUSED, new byte[4], portBytes));
        }
    }

    private boolean isBindRequest(ChannelBuffer msg) {
        if (msg.capacity() < 2)
            return false;

        if (msg.getByte(0) == VER_5 && msg.getByte(1) == Command.BIND.byteValue()) {
            return true;
        }
        return false;
    }

    private int b2i(byte b) {
        return (b < 0 ? 256 + b : b);
    }

    protected ChannelBuffer buildResponse(AddressType addressType, ResponseCode responseCode, byte[] ip, byte[] port) {
        ChannelBuffer response = HeapChannelBufferFactory.getInstance().getBuffer(10);

        response.writeByte(VER_5);
        response.writeByte(responseCode.byteValue());
        response.writeByte(RSV);
        response.writeByte(addressType.byteValue());
        if (addressType == AddressType.IPV4 || addressType == AddressType.DOMAINNAME) {
            response.writeBytes(ip);
            response.writeBytes(port);
        } else {
            throw new RuntimeException("Not supported");
        }

        return response;
    }
}
