package com.objectcode.lostsocks.client.config;

public interface IConfigurationHolder {
    String[] getProfiles();

    String getActiveProfile();

    IConfiguration getActiveConfiguration();

    void load();

    void save();
}
