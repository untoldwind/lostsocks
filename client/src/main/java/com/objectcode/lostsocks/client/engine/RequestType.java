package com.objectcode.lostsocks.client.engine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

public enum RequestType {
    VERSION_CHECK() {
        @Override
        public HttpRequest getHttpRequest(String uriBase, String connectionId, HttpEntity data) {
            HttpPost request = new HttpPost(uriBase + "/api/versionCheck");
            request.setEntity(data);
            return request;
        }
    },
    CONNECTION_CREATE() {
        @Override
        public HttpRequest getHttpRequest(String uriBase, String connectionId, HttpEntity data) {
            HttpPost request = new HttpPost(uriBase + "/api/connections");
            request.setEntity(data);
            return request;
        }
    },
    CONNECTION_REQUEST() {
        @Override
        public HttpRequest getHttpRequest(String uriBase, String connectionId, HttpEntity data) {
            HttpPut request = new HttpPut(uriBase + "/api/connections/" + connectionId);
            request.setEntity(data);
            return request;
        }
    },
    CONNECTION_CLOSE() {
        @Override
        public HttpRequest getHttpRequest(String uriBase, String connectionId, HttpEntity data) {
            HttpDelete request = new HttpDelete(uriBase + "/api/connections/" + connectionId);
            return request;
        }
    };

    public abstract HttpRequest getHttpRequest(String uriBase, String connectionId, HttpEntity data);
}
