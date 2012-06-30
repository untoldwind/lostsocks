package com.objectcode.lostsocks.client.config;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Realm;

import java.util.List;

public interface IConfiguration {
    long getTimeout();

    boolean isListenOnlyLocalhost();

    int getSocksPort();

    String getUrlString();

    void setUrlString(String url);

    String getUser();

    void setUser(String user);

    String getPassword();

    void setPassword(String password);

    boolean isUseProxy();

    String getProxyHost();

    String getProxyPort();

    String getProxyUser();

    boolean isProxyNeedsAuthentication();

    String getProxyPassword();

    Tunnel[] getTunnels();

    void setTunnels(Tunnel[] tunnels);

    void setProxyHost(String proxyHost);

    void setProxyNeedsAuthentication(boolean proxyNeedsAuthentication);

    void setProxyPassword(String proxyPassword);

    void setProxyPort(String proxyPort);

    void setProxyUser(String proxyUser);

    void setUseProxy(boolean useProxy);

    AsyncHttpClient createHttpClient();

    Realm getRealm();

    List<Network> getLocalNetworks();
}
