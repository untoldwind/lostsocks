package com.objectcode.lostsocks.client;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import com.objectcode.lostsocks.client.config.IConfigurationHolder;
import com.objectcode.lostsocks.client.config.PropertyFileConfigurationHolder;
import com.objectcode.lostsocks.client.swing.MainFrame;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author junglas
 */
public class Main {
    public static void main(String[] args) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);

        File configurationDir = new File(System.getProperty("user.home") + File.separator + ".sockstohttp");

        configurationDir.mkdirs();

        File configurationFile = new File(configurationDir, "config.properties");

        IConfigurationHolder configurationHolder = new PropertyFileConfigurationHolder(configurationFile);

        if (configurationFile.exists()) {
            configurationHolder.load();
        }

        MainFrame mainFrame = new MainFrame(configurationHolder);

        mainFrame.setVisible(true);
    }
}
