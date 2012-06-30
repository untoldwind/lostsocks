package com.objectcode.lostsocks.client.config;

import com.objectcode.lostsocks.client.utils.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class PropertyFileConfigurationHolder implements IConfigurationHolder {
    private static final Logger log = LoggerFactory.getLogger(PropertyFileConfigurationHolder.class);

    private File configurationFile;

    private String activeProfile;

    private List<String> profiles;

    private Map<String, PropertyFileConfiguration> configurations = new HashMap<String, PropertyFileConfiguration>();

    private boolean configurationChanged;

    public PropertyFileConfigurationHolder(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public List<String> getProfiles() {
        return profiles;
    }

    @Override
    public String getActiveProfile() {
        return activeProfile;
    }

    @Override
    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
        this.configurationChanged = true;
    }

    @Override
    public IConfiguration getActiveConfiguration() {
        return configurations.get(activeProfile);
    }

    @Override
    public IConfiguration addProfile(String profile) {
        profiles.add(profile);

        PropertyFileConfiguration configuration = new PropertyFileConfiguration(profile + ".");

        configurations.put(profile, configuration);

        this.configurationChanged = true;

        return configuration;
    }

    @Override
    public void load() {

        try {
            Properties properties = new Properties();

            properties.load(new FileInputStream(configurationFile));

            profiles = new ArrayList<String>(PropertiesHelper.getStringList(properties, "profiles", Collections.<String>emptyList()));
            activeProfile = PropertiesHelper.getString(properties, "activeProfile", null);

            PropertyFileConfiguration defaultConfiguration = new PropertyFileConfiguration("");

            defaultConfiguration.readProperties(properties);

            configurations.put(null, defaultConfiguration);

            for (String profile : profiles) {
                PropertyFileConfiguration configuration = new PropertyFileConfiguration(profile + ".");

                configuration.readProperties(properties);
                configurations.put(profile, configuration);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    @Override
    public void save() {
        for (PropertyFileConfiguration configuration : configurations.values()) {
            if (configuration.isConfigurationChanged()) {
                configurationChanged = true;
                break;
            }
        }
        if (!configurationChanged) {
            return;
        }

        configurationChanged = false;

        Properties properties = new Properties();

        PropertiesHelper.setStringList(properties, "profiles", profiles);
        PropertiesHelper.setString(properties, "activeProfile", activeProfile);

        for (PropertyFileConfiguration configuration : configurations.values()) {
            configuration.writeProperties(properties);
            configuration.setConfigurationChanged(false);
        }

        try {
            properties.store(new FileOutputStream(configurationFile), "Sock to HTTP");
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }
}
