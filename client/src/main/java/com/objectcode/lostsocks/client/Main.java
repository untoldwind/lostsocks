package com.objectcode.lostsocks.client;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.PersistenceServiceConfiguration;
import com.objectcode.lostsocks.client.config.PropertyFileConfiguration;
import com.objectcode.lostsocks.client.swing.MainFrame;

import javax.jnlp.BasicService;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import java.io.File;
import java.net.URL;

/**
 * @author junglas
 */
public class Main {
    public static void main(String[] args) {

        IConfiguration configuration = null;

        try {
            BasicService bs =
                    (BasicService) ServiceManager.lookup("javax.jnlp.BasicService");
            PersistenceService ps =
                    (PersistenceService) ServiceManager.lookup("javax.jnlp.PersistenceService");

            URL codebase = bs.getCodeBase();

            configuration = new PersistenceServiceConfiguration(ps, codebase);

            configuration.load();
        } catch (UnavailableServiceException ue) {
            // Service is not supported
        }

        if (configuration == null) {
            File configurationDir = new File(System.getProperty("user.home") + File.separator + ".sockstohttp");

            configurationDir.mkdirs();

            File configurationFile = new File(configurationDir, "config.properties");

            configuration = new PropertyFileConfiguration(configurationFile);

            if (configurationFile.exists()) {
                configuration.load();
            }
        }

        MainFrame mainFrame = new MainFrame(configuration);

        mainFrame.setVisible(true);
    }
}
