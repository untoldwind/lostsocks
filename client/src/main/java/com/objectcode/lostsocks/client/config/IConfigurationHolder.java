package com.objectcode.lostsocks.client.config;

import java.util.List;

public interface IConfigurationHolder {
    List<String> getProfiles();

    String getActiveProfile();

    void setActiveProfile(String activeProfile);

    IConfiguration getActiveConfiguration();

    IConfiguration addProfile(String profile);

    void load();

    void save();
}
