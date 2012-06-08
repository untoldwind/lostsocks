package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.PropertyFileConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedPacket {
    private final static Log log = LogFactory.getLog(PropertyFileConfiguration.class);

    private final byte[] data;
    private final boolean endOfCommunication;

    private final ContentType CONTENT_TYPE = ContentType.create("application/x-compressed-bytes");

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

    public HttpEntity toEntity() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream zos = new GZIPOutputStream(bos);
            DataOutputStream dos = new DataOutputStream(zos);
            dos.writeInt(data.length);
            dos.write(data);
            dos.writeBoolean(endOfCommunication);
            dos.flush();
            dos.close();
            return new ByteArrayEntity(bos.toByteArray(), CONTENT_TYPE);
        } catch (Exception e) {
            log.error("Encoding problem " + e, e);
            throw new RuntimeException(e);
        }
    }

    public static CompressedPacket fromEntity(HttpEntity entity) {
        try {
            GZIPInputStream gis = new GZIPInputStream(entity.getContent());
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
