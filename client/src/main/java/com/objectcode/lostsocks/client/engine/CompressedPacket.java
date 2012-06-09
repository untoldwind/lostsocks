package com.objectcode.lostsocks.client.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedPacket {
    private static final Logger log = LoggerFactory.getLogger(CompressedPacket.class);

    private final byte[] data;
    private final boolean endOfCommunication;

    public CompressedPacket(byte[] data, boolean endOfCommunication) {
        this.data = data;
        this.endOfCommunication = endOfCommunication;
    }

    public CompressedPacket(String dataStr, boolean endOfCommunication) {
        byte[] data = new byte[0];
        try {
            data = dataStr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding problem " + e, e);
        }
        this.data = data;
        this.endOfCommunication = endOfCommunication;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataAsString() {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding problem " + e, e);
        }
        return "";
    }

    public boolean isEndOfCommunication() {
        return endOfCommunication;
    }

    public byte[] toBody() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream dos = new DataOutputStream(zos);
            dos.writeInt(data.length);
            dos.write(data);
            dos.writeBoolean(endOfCommunication);
            dos.flush();
            dos.close();
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("Encoding problem " + e, e);
            throw new RuntimeException(e);
        }
    }


    public static CompressedPacket fromStream(InputStream in) {
        try {
            GZIPInputStream gis = new GZIPInputStream(in);
            DataInputStream dis = new DataInputStream(gis);
            int length = dis.readInt();
            byte[] data = new byte[length];
            dis.readFully(data);
            boolean endOfCommunication = dis.readBoolean();
            dis.close();

            return new CompressedPacket(data, endOfCommunication);
        } catch (Exception e) {
            log.error("Encoding problem " + e, e);
            throw new RuntimeException(e);
        }
    }

}
