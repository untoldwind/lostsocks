package com.objectcode.lostsocks.client.config;

import com.objectcode.lostsocks.client.utils.PropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyFileConfigurationHolder implements IConfigurationHolder {
    private static final Logger log = LoggerFactory.getLogger(PropertyFileConfigurationHolder.class);

    private File configurationFile;

    private String activeProfile;

    private String[] profiles;

    private Map<String, PropertyFileConfiguration> configurations = new HashMap<String, PropertyFileConfiguration>();

    public PropertyFileConfigurationHolder(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    @Override
    public String[] getProfiles() {
        return profiles;
    }

    @Override
    public String getActiveProfile() {
        return activeProfile;
    }

    @Override
    public IConfiguration getActiveConfiguration() {
        return configurations.get(activeProfile);
    }

    @Override
    public void load() {

        try {
            Properties properties = new Properties();

            properties.load(new FileInputStream(configurationFile));

            profiles = PropertiesHelper.getStrings(properties, "profiles", new String[0]);
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
        boolean configurationChanged = false;
        for (PropertyFileConfiguration configuration : configurations.values()) {
            if (configuration.isConfigurationChanged()) {
                configurationChanged = true;
                break;
            }
        }
        if (!configurationChanged) {
            return;
        }

        Properties properties = new Properties();

        PropertiesHelper.setStrings(properties, "profiles", profiles);
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
