package com.objectcode.lostsocks.client.config;

import java.net.InetAddress;

public class Network {
    byte[] network;
    byte[] mask;

    public Network(final InetAddress address, final short networkPrefixLength) {
        this.network = address.getAddress();
        this.mask = new byte[this.network.length];

        for ( int i = 0; i< network.length; i++ ) {
            byte current = 0;
            for ( int bit = 0 ; bit < 8 ; bit ++) {
                current <<= 1;
                if ( i * 8 + bit < networkPrefixLength)
                    current |= 0x1;
            }
            mask[i] = current;
            network[i] &= mask[i];
        }
    }

    public String getNetworkString() {
        return b2s(network);
    }

    public String getMaskString() {
        return b2s(mask);
    }

    public boolean match(byte[] address) {
        if ( address.length != network.length)
            return false;
        for ( int i = 0; i < network.length; i++ )
            if ( (address[i] & mask[i]) != (network[i] & mask[i]) )
                return false;
        return true;
    }

    @Override
    public String toString() {
        return "Network{" +
                "network=" + getNetworkString() +
                ", mask=" + getMaskString() +
                '}';
    }

    private String b2s(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        if ( bytes.length == 4 ) {
            for (int i = 0; i < bytes.length; i++ ) {
                if ( i > 0 )
                    result.append(".");
                result.append(Integer.toString(bytes[i] & 0xff));
            }
        } else {
            for (int i = 0; i < bytes.length; i++ ) {
                if ( i > 0 && i % 2 == 0 )
                    result.append(":");
                if ( (bytes[i] & 0xff) < 16)
                    result.append("0");
                result.append(Integer.toHexString(bytes[i] & 0xff));
            }
        }
        return result.toString();
    }

}
