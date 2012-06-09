package com.objectcode.lostsocks.client.config;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;

import java.net.URL;

/**
 * @author junglas
 */
public interface IConfiguration {
    public int getDelay();

    public long getTimeout();

    public int getMaxRetries();

    public long getDelayBetweenTries();

    public boolean isListenOnlyLocalhost();

    public long getForceRequestAfter();

    public long getDontTryToMinimizeTrafficBefore();

    public boolean isRequestOnlyIfClientActivity();

    public int getSocksPort();

    public URL getUrl();

    public String getUrlString();

    public void setUrlString(String url);

    public String getUser();

    public void setUser(String user);

    public String getPassword();

    public void setPassword(String password);

    public boolean isUseProxy();

    public String getProxyHost();

    public String getProxyPort();

    public String getProxyUser();

    public boolean isProxyNeedsAuthentication();

    public String getProxyPassword();

    public Tunnel[] getTunnels();

    public void setTunnels(Tunnel[] tunnels);

    public void setProxyHost(String proxyHost);

    public void setProxyNeedsAuthentication(boolean proxyNeedsAuthentication);

    public void setProxyPassword(String proxyPassword);

    public void setProxyPort(String proxyPort);

    public void setProxyUser(String proxyUser);

    public void setUseProxy(boolean useProxy);

    public void load();

    public void save();

    public HttpClient createHttpClient();

    public HttpHost getTargetHost();

    public String getTargetPath();
}
