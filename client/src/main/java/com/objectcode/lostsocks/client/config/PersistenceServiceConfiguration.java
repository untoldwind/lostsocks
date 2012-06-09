package com.objectcode.lostsocks.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Properties;

/**
 * @author junglas
 */
public class PersistenceServiceConfiguration extends PropertyFileConfiguration {
    private static final Logger log = LoggerFactory.getLogger(PersistenceServiceConfiguration.class);

    PersistenceService m_persistenceService;

    URL m_codebase;

    URL m_configurationUrl;

    public PersistenceServiceConfiguration(PersistenceService persistenceService, URL codebase) {

        super(null);

        m_persistenceService = persistenceService;
        m_codebase = codebase;
        try {
            m_configurationUrl = new URL(m_codebase, "config.properties");
            m_url = new URL(m_codebase, "socks");
            m_urlString = m_url.toString();
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    public void load() {

        try {
            FileContents fileContents = m_persistenceService.get(m_configurationUrl);

            Properties properties = new Properties();

            properties.load(fileContents.getInputStream());

            readProperties(properties);
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    public void save() {

        try {
            FileContents fileContents = null;

            try {
                fileContents = m_persistenceService.get(m_configurationUrl);
            } catch (FileNotFoundException e) {
                m_persistenceService.create(m_configurationUrl, 8192);
                fileContents = m_persistenceService.get(m_configurationUrl);
            }

            Properties properties = new Properties();

            writeProperties(properties);

            properties.store(fileContents.getOutputStream(true), "Socks to HTTP configuration");
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
}
