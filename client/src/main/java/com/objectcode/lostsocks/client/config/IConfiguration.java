package com.objectcode.lostsocks.client.config;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Realm;

import java.net.URL;
import java.util.List;

/**
 * @author junglas
 */
public interface IConfiguration {
    public int getDelay();

    long getTimeout();

    int getMaxRetries();

    long getDelayBetweenTries();

    boolean isListenOnlyLocalhost();

    long getForceRequestAfter();

    long getDontTryToMinimizeTrafficBefore();

    boolean isRequestOnlyIfClientActivity();

    int getSocksPort();

    URL getUrl();

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

    void load();

    void save();

    AsyncHttpClient createHttpClient();

    Realm getRealm();

    List<Network> getLocalNetworks();
}
