package com.objectcode.lostsocks.client.config;

import com.objectcode.lostsocks.client.utils.PropertiesHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author junglas
 * @created 28. Mai 2004
 */
public class PropertyFileConfiguration implements IConfiguration {
    private final static Log log = LogFactory.getLog(PropertyFileConfiguration.class);

    // Socks server settings
    private int m_port = 1080;

    private boolean m_listenOnlyLocalhost = true;

    // Tunneling settings
    protected String m_urlString = null;

    protected URL m_url = null;

    private String m_user = null;

    private String m_password = null;

    // Socks via HTTP Client settings
    private int m_delay = 20;

    // 20ms
    private boolean m_requestOnlyIfClientActivity = false;

    private long m_dontTryToMinimizeTrafficBefore = 10000;

    // 10s
    private long m_forceRequestAfter = 3000;
    // 3s

    // Resistance to HTTP request drops
    private int m_maxRetries = 0;

    private long m_delayBetweenTries = 3000;

    // Tunneling
    Tunnel[] m_tunnels = new Tunnel[0];

    // Socks via HTTP Server settings
    private long m_timeout = 0;

    // Proxy settings
    private boolean m_useProxy = false;

    private String m_proxyHost = null;

    private String m_proxyPort = null;

    private boolean m_proxyNeedsAuthentication = false;

    private String m_proxyUser = null;

    private String m_proxyPassword = null;

    private File m_configurationFile;

    private boolean m_configurationChanged = false;

    private HttpClient m_httpClient;

    private HttpHost m_targetHost;

    private String m_targetPath;

    /**
     * Constructor for the PropertyFileConfiguration object
     *
     * @param configurationFile Description of the Parameter
     */
    public PropertyFileConfiguration(File configurationFile) {

        m_configurationFile = configurationFile;

    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {

        if (m_password == null || !m_password.equals(password)) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_password = password;
    }

    /**
     * @param url The url to set.
     */
    public void setUrlString(String url) {

        if (m_urlString == null || !m_urlString.equals(url)) {
            m_configurationChanged = true;
        }
        m_urlString = url;
        try {
            if (m_urlString != null) {
                m_url = new URL(m_urlString);
            } else {
                m_url = null;
            }
            m_targetHost = null;
            m_targetPath = null;
        } catch (Exception e) {
            log.fatal("Exception", e);
        }
    }

    /**
     * @param user The user to set.
     */
    public void setUser(String user) {

        if (m_user == null || !m_user.equals(user)) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_user = user;
    }

    /**
     * @param tunnels The tunnels to set.
     */
    public void setTunnels(Tunnel[] tunnels) {

        m_tunnels = tunnels;

        m_configurationChanged = true;
    }

    /**
     * @return Returns the delay.
     */
    public int getDelay() {

        return m_delay;
    }

    /**
     * @return Returns the delayBetweenTries.
     */
    public long getDelayBetweenTries() {

        return m_delayBetweenTries;
    }

    /**
     * @return Returns the dontTryToMinimizeTrafficBefore.
     */
    public long getDontTryToMinimizeTrafficBefore() {

        return m_dontTryToMinimizeTrafficBefore;
    }

    /**
     * @return Returns the forceRequestAfter.
     */
    public long getForceRequestAfter() {

        return m_forceRequestAfter;
    }

    /**
     * @return Returns the listenOnlyLocalhost.
     */
    public boolean isListenOnlyLocalhost() {

        return m_listenOnlyLocalhost;
    }

    /**
     * @return Returns the maxRetries.
     */
    public int getMaxRetries() {

        return m_maxRetries;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {

        return m_password;
    }

    /**
     * @return Returns the port.
     */
    public int getPort() {

        return m_port;
    }

    /**
     * @return Returns the proxyHost.
     */
    public String getProxyHost() {

        return m_proxyHost;
    }

    /**
     * @return Returns the proxyNeedsAuthentication.
     */
    public boolean isProxyNeedsAuthentication() {

        return m_proxyNeedsAuthentication;
    }

    /**
     * @return Returns the proxyPassword.
     */
    public String getProxyPassword() {

        return m_proxyPassword;
    }

    /**
     * @return Returns the proxyPort.
     */
    public String getProxyPort() {

        return m_proxyPort;
    }

    /**
     * @return Returns the proxyUser.
     */
    public String getProxyUser() {

        return m_proxyUser;
    }

    /**
     * @return Returns the requestOnlyIfClientActivity.
     */
    public boolean isRequestOnlyIfClientActivity() {

        return m_requestOnlyIfClientActivity;
    }

    /**
     * @return Returns the timeout.
     */
    public long getTimeout() {

        return m_timeout;
    }

    /**
     * @return Returns the tunnels.
     */
    public Tunnel[] getTunnels() {

        return m_tunnels;
    }

    /**
     * @return Returns the url.
     */
    public String getUrlString() {

        return m_urlString;
    }

    /**
     * @return Returns the useProxy.
     */
    public boolean isUseProxy() {

        return m_useProxy;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {

        return m_user;
    }

    /**
     * @return Returns the url.
     */
    public URL getUrl() {

        return m_url;
    }

    /**
     * @param proxyHost The proxyHost to set.
     */
    public void setProxyHost(String proxyHost) {

        if (m_proxyHost == null || !m_proxyHost.equals(proxyHost)) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_proxyHost = proxyHost;
    }

    /**
     * @param proxyNeedsAuthentication The proxyNeedsAuthentication to set.
     */
    public void setProxyNeedsAuthentication(boolean proxyNeedsAuthentication) {

        if (m_proxyNeedsAuthentication != proxyNeedsAuthentication) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_proxyNeedsAuthentication = proxyNeedsAuthentication;
    }

    /**
     * @param proxyPassword The proxyPassword to set.
     */
    public void setProxyPassword(String proxyPassword) {

        if (m_proxyPassword == null || !m_proxyPassword.equals(proxyPassword)) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_proxyPassword = proxyPassword;
    }

    /**
     * @param proxyPort The proxyPort to set.
     */
    public void setProxyPort(String proxyPort) {

        if (m_proxyPort == null || !m_proxyPort.equals(proxyPort)) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_proxyPort = proxyPort;
    }

    /**
     * @param proxyUser The proxyUser to set.
     */
    public void setProxyUser(String proxyUser) {

        if (m_proxyUser == null || !m_proxyUser.equals(proxyUser)) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_proxyUser = proxyUser;
    }

    /**
     * @param useProxy The useProxy to set.
     */
    public void setUseProxy(boolean useProxy) {

        if (m_useProxy != useProxy) {
            m_configurationChanged = true;
            m_httpClient = null;
        }
        m_useProxy = useProxy;
    }

    public HttpHost getTargetHost() {
        if (m_targetHost == null) {
            m_targetHost = new HttpHost(m_url.getHost(), m_url.getPort(), m_url.getProtocol());
            m_targetPath = m_url.getPath();
            if (m_targetPath.endsWith("/"))
                m_targetPath = m_targetPath.substring(0, m_targetPath.length() - 1);
        }
        return m_targetHost;
    }

    public String getTargetPath() {
        if (m_targetPath == null) {
            m_targetHost = new HttpHost(m_url.getHost(), m_url.getPort(), m_url.getProtocol());
            m_targetPath = m_url.getPath();
            if (m_targetPath.endsWith("/"))
                m_targetPath = m_targetPath.substring(0, m_targetPath.length() - 1);
        }
        return m_targetPath;
    }

    public HttpClient createHttpClient() {

        if (m_httpClient == null) {
            synchronized (this) {
                PoolingClientConnectionManager clientConnectionManager = new PoolingClientConnectionManager();
                DefaultHttpClient httpClient = new DefaultHttpClient(clientConnectionManager);

                httpClient.getCredentialsProvider().setCredentials(
                        new AuthScope(getTargetHost().getHostName(), getTargetHost().getPort()),
                        new UsernamePasswordCredentials(m_user, m_password));

                if (m_useProxy) {
                    HttpHost proxy = new HttpHost(m_proxyHost, Integer.parseInt(m_proxyPort));
                    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                    if (m_proxyNeedsAuthentication) {
                        List<String> authpref = new ArrayList<String>();
                        authpref.add(AuthPolicy.BASIC);
                        authpref.add(AuthPolicy.DIGEST);
                        httpClient.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF,
                                authpref);
                        httpClient.getCredentialsProvider().setCredentials(
                                new AuthScope(proxy.getHostName(), proxy.getPort()),
                                new UsernamePasswordCredentials(m_proxyUser, m_proxyPassword));

                    }
                }
                m_httpClient = httpClient;
            }
        }

        return m_httpClient;
    }

    /**
     * Description of the Method
     */
    public void load() {

        try {
            Properties properties = new Properties();

            properties.load(new FileInputStream(m_configurationFile));

            readProperties(properties);

            m_configurationChanged = false;
        } catch (Exception e) {
            log.fatal("Exception", e);
        }
    }

    /**
     * Description of the Method
     *
     * @param properties Description of the Parameter
     */
    protected void readProperties(Properties properties) {
        // Socks server access configuration
        m_urlString = PropertiesHelper.getString(properties, "socks.http.servlet.url", null);
        try {
            m_url = new URL(m_urlString);
        } catch (Exception e) {
            log.fatal("Exception", e);
        }
        m_port = PropertiesHelper.getInt(properties, "socks.server.port", m_port);

        m_listenOnlyLocalhost =
                PropertiesHelper.getBoolean(properties, "socks.listen.localhost", m_listenOnlyLocalhost);
        m_user = PropertiesHelper.getString(properties, "socks.httpserver.user", m_user);
        m_password = PasswordEncoder
                .decodePassword(PropertiesHelper.getString(properties, "socks.httpserver.password", m_password));

        m_delay = PropertiesHelper.getInt(properties, "socks.delay", m_delay);
        m_requestOnlyIfClientActivity = PropertiesHelper
                .getBoolean(properties, "socks.requestonlyifclientactivity", m_requestOnlyIfClientActivity);
        m_dontTryToMinimizeTrafficBefore = PropertiesHelper
                .getLong(properties, "socks.donttrytominimizetrafficbefore", m_dontTryToMinimizeTrafficBefore);
        m_forceRequestAfter = PropertiesHelper.getLong(properties, "socks.forcerequestafter", m_forceRequestAfter);

        m_maxRetries = PropertiesHelper.getInt(properties, "socks.maxretries", m_maxRetries);
        m_delayBetweenTries = PropertiesHelper.getLong(properties, "socks.delaybetweenretries", m_delayBetweenTries);

        m_timeout = PropertiesHelper.getLong(properties, "socks.httpserver.timeout", m_timeout);

        // Tunneling settings
        String[] sActivePorts = PropertiesHelper.getStrings(properties, "tunnel.ports.active", new String[0]);
        m_tunnels = new Tunnel[sActivePorts.length];
        for (int i = 0; i < sActivePorts.length; i++) {
            int localPort = Integer.parseInt(sActivePorts[i]);
            String destinationUri = PropertiesHelper.getString(properties, "tunnel.localport." + localPort, null);
            m_tunnels[i] = new Tunnel(localPort, destinationUri);
        }

        // Proxy settings
        m_useProxy = PropertiesHelper.getBoolean(properties, "socks.proxy", m_useProxy);
        m_proxyHost = PropertiesHelper.getString(properties, "socks.proxy.host", m_proxyHost);
        m_proxyPort = PropertiesHelper.getString(properties, "socks.proxy.port", m_proxyPort);
        m_proxyNeedsAuthentication =
                PropertiesHelper.getBoolean(properties, "socks.proxy.authentication", m_proxyNeedsAuthentication);
        m_proxyUser = PropertiesHelper.getString(properties, "socks.proxy.user", m_proxyUser);
        m_proxyPassword = PasswordEncoder
                .decodePassword(PropertiesHelper.getString(properties, "socks.proxy.password", m_proxyPassword));
    }

    /**
     * Description of the Method
     */
    public void save() {

        if (!m_configurationChanged) {
            return;
        }

        Properties properties = new Properties();

        writeProperties(properties);

        try {
            properties.store(new FileOutputStream(m_configurationFile), "Sock to HTTP");
        } catch (Exception e) {
            log.fatal("Exception", e);
        }
        m_configurationChanged = false;
    }

    /**
     * Description of the Method
     *
     * @param properties Description of the Parameter
     */
    protected void writeProperties(Properties properties) {

        PropertiesHelper.setString(properties, "socks.http.servlet.url", m_urlString);
        properties.setProperty("socks.server.port", Integer.toString(m_port));
        properties.setProperty("socks.listen.localhost", Boolean.toString(m_listenOnlyLocalhost));
        PropertiesHelper.setString(properties, "socks.httpserver.user", m_user);
        PropertiesHelper.setString(properties, "socks.httpserver.password", PasswordEncoder.encodePassword(m_password));
        properties.setProperty("socks.delay", Integer.toString(m_delay));
        properties.setProperty("socks.requestonlyifclientactivity", Boolean.toString(m_requestOnlyIfClientActivity));
        properties.setProperty("socks.donttrytominimizetrafficbefore", Long.toString(m_dontTryToMinimizeTrafficBefore));
        properties.setProperty("socks.forcerequestafter", Long.toString(m_forceRequestAfter));
        properties.setProperty("socks.maxretries", Long.toString(m_maxRetries));
        properties.setProperty("socks.delaybetweenretries", Long.toString(m_delayBetweenTries));

        StringBuffer sActivePorts = new StringBuffer();
        int i;

        for (i = 0; i < m_tunnels.length; i++) {
            if (i > 0) {
                sActivePorts.append(",");
            }
            sActivePorts.append(m_tunnels[i].getLocalPort());
            properties.setProperty("tunnel.localport." + m_tunnels[i].getLocalPort(), m_tunnels[i].getDestinationUri());
        }
        properties.setProperty("tunnel.ports.active", sActivePorts.toString());

        PropertiesHelper.setString(properties, "socks.proxy", Boolean.toString(m_useProxy));
        PropertiesHelper.setString(properties, "socks.proxy.host", m_proxyHost);
        PropertiesHelper.setString(properties, "socks.proxy.port", m_proxyPort);
        properties.setProperty("socks.proxy.authentication", Boolean.toString(m_proxyNeedsAuthentication));
        PropertiesHelper.setString(properties, "socks.proxy.user", m_proxyUser);
        PropertiesHelper.setString(properties, "socks.proxy.password", PasswordEncoder.encodePassword(m_proxyPassword));
    }
}
