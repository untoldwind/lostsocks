package com.objectcode.lostsocks.client;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.PropertyFileConfiguration;
import com.objectcode.lostsocks.client.swing.MainFrame;

import java.io.File;

/**
 * @author junglas
 */
public class Main {
    public static void main(String[] args) {

        File configurationDir = new File(System.getProperty("user.home") + File.separator + ".sockstohttp");

        configurationDir.mkdirs();

        File configurationFile = new File(configurationDir, "config.properties");

        IConfiguration configuration = new PropertyFileConfiguration(configurationFile);

        if (configurationFile.exists()) {
            configuration.load();
        }

        MainFrame mainFrame = new MainFrame(configuration);

        mainFrame.setVisible(true);
    }
}
