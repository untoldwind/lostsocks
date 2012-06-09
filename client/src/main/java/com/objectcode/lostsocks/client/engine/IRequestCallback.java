package com.objectcode.lostsocks.client.engine;

public interface IRequestCallback {
    void onSuccess(CompressedPacket result);

    void onFailure(int statusCode, String statusText);
}
