package com.objectcode.lostsocks.client.config;

import com.objectcode.lostsocks.client.utils.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
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

    public void setTunnels(Tunnel[] tunnels) {
        this.tunnels = tunnels;

        configurationChanged = true;
    }

    @Override
    public boolean isListenOnlyLocalhost() {
        return listenOnlyLocalhost;
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
        urlString = PropertiesHelper.getString(properties, prefix + "socks.http.servlet.url", null);
        try {
            url = new URL(urlString);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        port = PropertiesHelper.getInt(properties, prefix + "socks.server.port", port);

        listenOnlyLocalhost =
                PropertiesHelper.getBoolean(properties, prefix + "socks.listen.localhost", listenOnlyLocalhost);
        user = PropertiesHelper.getString(properties, prefix + "socks.httpserver.user", user);
        password = PasswordEncoder
                .decodePassword(PropertiesHelper.getString(properties, prefix + "socks.httpserver.password", password));

        delay = PropertiesHelper.getInt(properties, prefix + "socks.delay", delay);
        requestOnlyIfClientActivity = PropertiesHelper
                .getBoolean(properties, prefix + "socks.requestonlyifclientactivity", requestOnlyIfClientActivity);
        dontTryToMinimizeTrafficBefore = PropertiesHelper
                .getLong(properties, prefix + "socks.donttrytominimizetrafficbefore", dontTryToMinimizeTrafficBefore);
        forceRequestAfter = PropertiesHelper.getLong(properties, prefix + "socks.forcerequestafter", forceRequestAfter);

        maxRetries = PropertiesHelper.getInt(properties, "prefix + socks.maxretries", maxRetries);
        delayBetweenTries = PropertiesHelper.getLong(properties, prefix + "socks.delaybetweenretries", delayBetweenTries);

        timeout = PropertiesHelper.getLong(properties, prefix + "socks.httpserver.timeout", timeout);

        // Tunneling settings
        String[] sActivePorts = PropertiesHelper.getStrings(properties, prefix + "tunnel.ports.active", new String[0]);
        tunnels = new Tunnel[sActivePorts.length];
        for (int i = 0; i < sActivePorts.length; i++) {
            int localPort = Integer.parseInt(sActivePorts[i]);
            String destinationUri = PropertiesHelper.getString(properties, prefix + "tunnel.localport." + localPort, null);
            tunnels[i] = new Tunnel(localPort, destinationUri);
        }

        // Proxy settings
        useProxy = PropertiesHelper.getBoolean(properties, prefix + "socks.proxy", useProxy);
        proxyHost = PropertiesHelper.getString(properties, prefix + "socks.proxy.host", proxyHost);
        proxyPort = PropertiesHelper.getString(properties, prefix + "socks.proxy.port", proxyPort);
        proxyNeedsAuthentication =
                PropertiesHelper.getBoolean(properties, prefix + "socks.proxy.authentication", proxyNeedsAuthentication);
        proxyUser = PropertiesHelper.getString(properties, prefix + "socks.proxy.user", proxyUser);
        proxyPassword = PasswordEncoder
                .decodePassword(PropertiesHelper.getString(properties, prefix + "socks.proxy.password", proxyPassword));

        int numLocalNetworks = PropertiesHelper.getInt(properties, prefix + "numLocalNetworks", 0);

        localNetworks.clear();
        for ( int i = 0; i < numLocalNetworks; i++ ) {
            String wildcard = PropertiesHelper.getString(properties, prefix + "localNetwork." + i, null);

            if ( wildcard != null )
                localNetworks.add(new SimpleWildcard(wildcard));
        }
    }


    protected void writeProperties(Properties properties) {
        PropertiesHelper.setString(properties, prefix + "socks.http.servlet.url", urlString);
        properties.setProperty(prefix + "socks.server.port", Integer.toString(port));
        properties.setProperty(prefix + "socks.listen.localhost", Boolean.toString(listenOnlyLocalhost));
        PropertiesHelper.setString(properties, prefix + "socks.httpserver.user", user);
        PropertiesHelper.setString(properties, prefix + "socks.httpserver.password", PasswordEncoder.encodePassword(password));
        properties.setProperty(prefix + "socks.delay", Integer.toString(delay));
        properties.setProperty(prefix + "socks.requestonlyifclientactivity", Boolean.toString(requestOnlyIfClientActivity));
        properties.setProperty(prefix + "socks.donttrytominimizetrafficbefore", Long.toString(dontTryToMinimizeTrafficBefore));
        properties.setProperty(prefix + "socks.forcerequestafter", Long.toString(forceRequestAfter));
        properties.setProperty(prefix + "socks.maxretries", Long.toString(maxRetries));
        properties.setProperty(prefix + "socks.delaybetweenretries", Long.toString(delayBetweenTries));

        StringBuilder sActivePorts = new StringBuilder();
        int i;

        for (i = 0; i < tunnels.length; i++) {
            if (i > 0) {
                sActivePorts.append(",");
            }
            sActivePorts.append(tunnels[i].getLocalPort());
            properties.setProperty(prefix + "tunnel.localport." + tunnels[i].getLocalPort(), tunnels[i].getDestinationUri());
        }
        properties.setProperty(prefix + "tunnel.ports.active", sActivePorts.toString());

        PropertiesHelper.setString(properties, prefix + "socks.proxy", Boolean.toString(useProxy));
        PropertiesHelper.setString(properties, prefix + "socks.proxy.host", proxyHost);
        PropertiesHelper.setString(properties, prefix + "socks.proxy.port", proxyPort);
        properties.setProperty(prefix + "socks.proxy.authentication", Boolean.toString(proxyNeedsAuthentication));
        PropertiesHelper.setString(properties, prefix + "socks.proxy.user", proxyUser);
        PropertiesHelper.setString(properties, prefix + "socks.proxy.password", PasswordEncoder.encodePassword(proxyPassword));

        PropertiesHelper.setInt(properties, prefix + "numLocalNetworks", localNetworks.size());

        for ( i = 0; i < localNetworks.size(); i++ ) {
            PropertiesHelper.setString(properties, prefix + "localNetwork." + i, localNetworks.get(i).getWildcard());
        }
    }
}
