package com.objectcode.lostsocks.client.engine;

public enum RequestType {
    VERSION_CHECK("/api/versionCheck");

    private final String uri;

    private RequestType(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
