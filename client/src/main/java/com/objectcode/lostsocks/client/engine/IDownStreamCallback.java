package com.objectcode.lostsocks.client.engine;

public interface IDownStreamCallback {
    void sendData(byte[] data);

    void sendEOF();
}
