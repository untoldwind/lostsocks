package com.objectcode.lostsocks.client.config;

import com.objectcode.lostsocks.client.utils.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PropertyFileConfiguration extends BasicConfiguration {
    private static final Logger log = LoggerFactory.getLogger(PropertyFileConfiguration.class);

    private String prefix;

    // Socks server settings
    private int port = 1080;

    private boolean listenOnlyLocalhost = true;

    // Tunneling settings
    protected String urlString = null;

    protected URL url = null;

    private String user = null;

    private String password = null;

    // Socks via HTTP Client settings
    private int delay = 20;

    // 20ms
    private boolean requestOnlyIfClientActivity = false;

    private long dontTryToMinimizeTrafficBefore = 10000;

    // 10s
    private long forceRequestAfter = 3000;
    // 3s

    // Resistance to HTTP request drops
    private int maxRetries = 0;

    private long delayBetweenTries = 3000;

    // Tunneling
    Tunnel[] tunnels = new Tunnel[0];

    // Socks via HTTP Server settings
    private long timeout = 0;

    // Proxy settings
    private boolean useProxy = false;

    private String proxyHost = null;

    private String proxyPort = null;

    private boolean proxyNeedsAuthentication = false;

    private String proxyUser = null;

    private String proxyPassword = null;

    private boolean configurationChanged = false;

    public PropertyFileConfiguration(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void setPassword(String password) {
        if (this.password == null || !this.password.equals(password)) {
            configurationChanged = true;
            httpClient = null;
        }
        this.password = password;
    }

    @Override
    public void setUrlString(String url) {
        if (urlString == null || !urlString.equals(url)) {
            configurationChanged = true;
        }
        urlString = url;
        try {
            if (urlString != null) {
                if (urlString.endsWith("/"))
                    urlString = urlString.substring(0, urlString.length() - 1);
                this.url = new URL(urlString);
            } else {
                this.url = null;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    @Override
    public void setUser(String user) {

        if (this.user == null || !this.user.equals(user)) {
            configurationChanged = true;
            httpClient = null;
        }
        this.user = user;
    }

    @Override
    public void setTunnels(Tunnel[] tunnels) {
        this.tunnels = tunnels;

        configurationChanged = true;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public long getDelayBetweenTries() {
        return delayBetweenTries;
    }

    @Override
    public long getDontTryToMinimizeTrafficBefore() {
        return dontTryToMinimizeTrafficBefore;
    }

    @Override
    public long getForceRequestAfter() {
        return forceRequestAfter;
    }

    @Override
    public boolean isListenOnlyLocalhost() {
        return listenOnlyLocalhost;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getSocksPort() {
        return port;
    }

    @Override
    public String getProxyHost() {
        return proxyHost;
    }

    @Override
    public boolean isProxyNeedsAuthentication() {
        return proxyNeedsAuthentication;
    }

    @Override
    public String getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public String getProxyPort() {
        return proxyPort;
    }

    @Override
    public String getProxyUser() {
        return proxyUser;
    }

    @Override
    public boolean isRequestOnlyIfClientActivity() {
        return requestOnlyIfClientActivity;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public Tunnel[] getTunnels() {
        return tunnels;
    }

    @Override
    public String getUrlString() {
        return urlString;
    }

    @Override
    public boolean isUseProxy() {
        return useProxy;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setProxyHost(String proxyHost) {
        if (this.proxyHost == null || !this.proxyHost.equals(proxyHost)) {
            configurationChanged = true;
            httpClient = null;
        }
        this.proxyHost = proxyHost;
    }

    @Override
    public void setProxyNeedsAuthentication(boolean proxyNeedsAuthentication) {

        if (this.proxyNeedsAuthentication != proxyNeedsAuthentication) {
            configurationChanged = true;
            httpClient = null;
        }
        this.proxyNeedsAuthentication = proxyNeedsAuthentication;
    }

    @Override
    public void setProxyPassword(String proxyPassword) {
        if (this.proxyPassword == null || !this.proxyPassword.equals(proxyPassword)) {
            configurationChanged = true;
            httpClient = null;
        }
        this.proxyPassword = proxyPassword != null && proxyPassword.length() > 0 ? proxyPassword : null;
    }

    @Override
    public void setProxyPort(String proxyPort) {
        if (this.proxyPort == null || !this.proxyPort.equals(proxyPort)) {
            configurationChanged = true;
            httpClient = null;
        }
        this.proxyPort = proxyPort;
    }

    @Override
    public void setProxyUser(String proxyUser) {
        if (this.proxyUser == null || !this.proxyUser.equals(proxyUser)) {
            configurationChanged = true;
            httpClient = null;
        }
        this.proxyUser = proxyUser != null && proxyUser.length() > 0 ? proxyUser : null;
    }

    @Override
    public void setUseProxy(boolean useProxy) {
        if (this.useProxy != useProxy) {
            configurationChanged = true;
            httpClient = null;
        }
        this.useProxy = useProxy;
    }

    public boolean isConfigurationChanged() {
        return configurationChanged;
    }

    public void setConfigurationChanged(boolean configurationChanged) {
        this.configurationChanged = configurationChanged;
    }

    protected void readProperties(Properties properties) {
        // Socks server access configuration
        urlString = PropertiesHelper.getString(properties, "socks.http.servlet.url", null);
        try {
            url = new URL(urlString);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        port = PropertiesHelper.getInt(properties, "socks.server.port", port);

        listenOnlyLocalhost =
                PropertiesHelper.getBoolean(properties, "socks.listen.localhost", listenOnlyLocalhost);
        user = PropertiesHelper.getString(properties, "socks.httpserver.user", user);
        password = PasswordEncoder
                .decodePassword(PropertiesHelper.getString(properties, "socks.httpserver.password", password));

        delay = PropertiesHelper.getInt(properties, "socks.delay", delay);
        requestOnlyIfClientActivity = PropertiesHelper
                .getBoolean(properties, "socks.requestonlyifclientactivity", requestOnlyIfClientActivity);
        dontTryToMinimizeTrafficBefore = PropertiesHelper
                .getLong(properties, "socks.donttrytominimizetrafficbefore", dontTryToMinimizeTrafficBefore);
        forceRequestAfter = PropertiesHelper.getLong(properties, "socks.forcerequestafter", forceRequestAfter);

        maxRetries = PropertiesHelper.getInt(properties, "socks.maxretries", maxRetries);
        delayBetweenTries = PropertiesHelper.getLong(properties, "socks.delaybetweenretries", delayBetweenTries);

        timeout = PropertiesHelper.getLong(properties, "socks.httpserver.timeout", timeout);

        // Tunneling settings
        String[] sActivePorts = PropertiesHelper.getStrings(properties, "tunnel.ports.active", new String[0]);
        tunnels = new Tunnel[sActivePorts.length];
        for (int i = 0; i < sActivePorts.length; i++) {
            int localPort = Integer.parseInt(sActivePorts[i]);
            String destinationUri = PropertiesHelper.getString(properties, "tunnel.localport." + localPort, null);
            tunnels[i] = new Tunnel(localPort, destinationUri);
        }

        // Proxy settings
        useProxy = PropertiesHelper.getBoolean(properties, "socks.proxy", useProxy);
        proxyHost = PropertiesHelper.getString(properties, "socks.proxy.host", proxyHost);
        proxyPort = PropertiesHelper.getString(properties, "socks.proxy.port", proxyPort);
        proxyNeedsAuthentication =
                PropertiesHelper.getBoolean(properties, "socks.proxy.authentication", proxyNeedsAuthentication);
        proxyUser = PropertiesHelper.getString(properties, "socks.proxy.user", proxyUser);
        proxyPassword = PasswordEncoder
                .decodePassword(PropertiesHelper.getString(properties, "socks.proxy.password", proxyPassword));

        try {
            List<Network> localNetworks = new ArrayList<Network>();
            Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces();
            while (it.hasMoreElements()) {
                NetworkInterface networkIf = it.nextElement();
                for (InterfaceAddress ifAddress : networkIf.getInterfaceAddresses()) {
                    if (!ifAddress.getAddress().isAnyLocalAddress() && !ifAddress.getAddress().isLoopbackAddress()) {
                        localNetworks.add(new Network(ifAddress.getAddress(), ifAddress.getNetworkPrefixLength()));
                    }
                }
            }
            this.localNetworks = localNetworks;
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }


    protected void writeProperties(Properties properties) {

        PropertiesHelper.setString(properties, "socks.http.servlet.url", urlString);
        properties.setProperty("socks.server.port", Integer.toString(port));
        properties.setProperty("socks.listen.localhost", Boolean.toString(listenOnlyLocalhost));
        PropertiesHelper.setString(properties, "socks.httpserver.user", user);
        PropertiesHelper.setString(properties, "socks.httpserver.password", PasswordEncoder.encodePassword(password));
        properties.setProperty("socks.delay", Integer.toString(delay));
        properties.setProperty("socks.requestonlyifclientactivity", Boolean.toString(requestOnlyIfClientActivity));
        properties.setProperty("socks.donttrytominimizetrafficbefore", Long.toString(dontTryToMinimizeTrafficBefore));
        properties.setProperty("socks.forcerequestafter", Long.toString(forceRequestAfter));
        properties.setProperty("socks.maxretries", Long.toString(maxRetries));
        properties.setProperty("socks.delaybetweenretries", Long.toString(delayBetweenTries));

        StringBuffer sActivePorts = new StringBuffer();
        int i;

        for (i = 0; i < tunnels.length; i++) {
            if (i > 0) {
                sActivePorts.append(",");
            }
            sActivePorts.append(tunnels[i].getLocalPort());
            properties.setProperty("tunnel.localport." + tunnels[i].getLocalPort(), tunnels[i].getDestinationUri());
        }
        properties.setProperty("tunnel.ports.active", sActivePorts.toString());

        PropertiesHelper.setString(properties, "socks.proxy", Boolean.toString(useProxy));
        PropertiesHelper.setString(properties, "socks.proxy.host", proxyHost);
        PropertiesHelper.setString(properties, "socks.proxy.port", proxyPort);
        properties.setProperty("socks.proxy.authentication", Boolean.toString(proxyNeedsAuthentication));
        PropertiesHelper.setString(properties, "socks.proxy.user", proxyUser);
        PropertiesHelper.setString(properties, "socks.proxy.password", PasswordEncoder.encodePassword(proxyPassword));
    }
}
