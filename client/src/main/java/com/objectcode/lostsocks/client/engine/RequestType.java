package com.objectcode.lostsocks.client.engine;

import com.ning.http.client.RequestBuilder;
import org.jboss.netty.handler.codec.http.HttpHeaders;

public enum RequestType {
    VERSION_CHECK() {
        @Override
        public RequestBuilder getHttpRequest(String urlBase, String connectionId, byte[] data) {
            RequestBuilder builder = new RequestBuilder("POST");
            builder.setUrl(urlBase + "/api/versionCheck");
            builder.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-compressed-bytes");
            builder.setBody(data);
            return builder;
        }
    },
    CONNECTION_CREATE() {
        @Override
        public RequestBuilder getHttpRequest(String urlBase, String connectionId, byte[] data) {
            RequestBuilder builder = new RequestBuilder("POST");
            builder.setUrl(urlBase + "/api/connections");
            builder.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-compressed-bytes");
            builder.setBody(data);
            return builder;
        }
    },
    CONNECTION_REQUEST() {
        @Override
        public RequestBuilder getHttpRequest(String urlBase, String connectionId, byte[] data) {
            RequestBuilder builder = new RequestBuilder("PUT");
            builder.setUrl(urlBase + "/api/connections/" + connectionId);
            builder.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-compressed-bytes");
            builder.setBody(data);
            return builder;
        }
    },
    CONNECTION_GET() {
        @Override
        public RequestBuilder getHttpRequest(String urlBase, String connectionId, byte[] data) {
            RequestBuilder builder = new RequestBuilder("GET");
            builder.setUrl(urlBase + "/api/connections/" + connectionId);
            return builder;
        }
    },
    CONNECTION_CLOSE() {
        @Override
        public RequestBuilder getHttpRequest(String urlBase, String connectionId, byte[] data) {
            RequestBuilder builder = new RequestBuilder("DELETE");
            builder.setUrl(urlBase + "/api/connections/" + connectionId);
            return builder;
        }
    };

    public abstract RequestBuilder getHttpRequest(String urlBase, String connectionId, byte[] data);
}
