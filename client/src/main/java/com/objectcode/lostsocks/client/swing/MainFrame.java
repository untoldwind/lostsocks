package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.IConfigurationHolder;
import com.objectcode.lostsocks.client.engine.NIOSocksServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = -5199207523089060681L;

    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

    private JComboBox comboBoxField;
    private JButton addProfileButton;
    private JTextField serverUrlField;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton startSocksButton;
    private JButton stopSocksButton;
    private JButton addTunnelButton;
    private JButton removeTunnelButton;
    private JButton startTunnelButton;
    private JButton stopTunnelButton;
    private JButton httpProxyButton;
    private JButton localNetworksButton;
    private JTable tunnelTable;
    private TunnelsTableModel tunnelTableModel;

    private NIOSocksServer socksServer;

    private IConfigurationHolder configurationHolder;
    private IConfiguration configuration;


    public MainFrame(IConfigurationHolder configurationHolder) {
        super("Socks to HTTP");

        addWindowListener(new WindowCloseListener());

        this.configurationHolder = configurationHolder;
        this.configuration = this.configurationHolder.getActiveConfiguration();

        comboBoxField = new JComboBox();
        comboBoxField.setEditable(true);
        addProfileButton = new JButton("Add Profile");
        serverUrlField = new JTextField(this.configuration.getUrlString());
        userField = new JTextField(this.configuration.getUser());
        passwordField = new JPasswordField(this.configuration.getPassword());
        startSocksButton = new JButton("Start Socks");
        startSocksButton.addActionListener(new StartSocksListener());
        stopSocksButton = new JButton("Stop Socks");
        stopSocksButton.setEnabled(false);
        stopSocksButton.addActionListener(new StopSocksListener());
        httpProxyButton = new JButton("HTTP Proxy");
        httpProxyButton.addActionListener(new HttpProxyListener());
        localNetworksButton = new JButton("Local networks");
        localNetworksButton.addActionListener(new LocalNetworksListener());
        addTunnelButton = new JButton("Add Tunnel");
        addTunnelButton.addActionListener(new AddTunnelListener());
        removeTunnelButton = new JButton("Remove Tunnel");
        removeTunnelButton.addActionListener(new RemoveTunnelListener());
        removeTunnelButton.setEnabled(false);
        startTunnelButton = new JButton("Start Tunnel");
        startTunnelButton.addActionListener(new StartTunnelListener());
        startTunnelButton.setEnabled(false);
        stopTunnelButton = new JButton("Stop Tunnel");
        stopTunnelButton.addActionListener(new StopTunnelListener());
        stopTunnelButton.setEnabled(false);
        tunnelTableModel = new TunnelsTableModel(this.configuration.getTunnels());
        tunnelTable = new JTable(tunnelTableModel);
        tunnelTable.getSelectionModel().addListSelectionListener(new TunnelTableListener());

        initComponents();

        // Proxy on
        if (configuration.isUseProxy()) {
            // Logging
            log.info("Using proxy " + configuration.getProxyHost() + ":" + configuration.getProxyPort());

            // JDK 1.3 and below
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", configuration.getProxyHost());
            System.getProperties().put("proxyPort", configuration.getProxyPort());

            // JDK 1.4
            System.getProperties().put("http.proxyHost", configuration.getProxyHost());
            System.getProperties().put("http.proxyPort", configuration.getProxyPort());
        } else {
            // JDK 1.3 and below
            System.getProperties().remove("proxySet");
            System.getProperties().remove("proxyHost");
            System.getProperties().remove("proxyPort");

            // JDK 1.4
            System.getProperties().remove("http.proxyHost");
            System.getProperties().remove("http.proxyPort");
        }
    }


    /**
     * Description of the Method
     */
    void updateConfiguration() {
        configuration.setUrlString(serverUrlField.getText());
        configuration.setUser(userField.getText());
        configuration.setPassword(new String(passwordField.getPassword()));

        configurationHolder.save();
    }


    /**
     * Description of the Method
     */
    protected void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());

        getContentPane().add(root, BorderLayout.CENTER);

        root.add(new JLabel("Profile:"),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(comboBoxField,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(addProfileButton,
                new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("URL:"),
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(serverUrlField,
                new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("User:"),
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(userField,
                new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Password:"),
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(passwordField,
                new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        root.add(httpProxyButton,
                new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(localNetworksButton,
                new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        root.add(buttonPanel,
                new GridBagConstraints(0, 4, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        buttonPanel.add(startSocksButton,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        buttonPanel.add(stopSocksButton,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        JScrollPane scrollPane = new JScrollPane(tunnelTable);

        root.add(scrollPane,
                new GridBagConstraints(0, 5, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        JPanel buttonPanel2 = new JPanel(new GridBagLayout());

        root.add(buttonPanel2,
                new GridBagConstraints(0, 6, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        buttonPanel2.add(addTunnelButton,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        buttonPanel2.add(removeTunnelButton,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        buttonPanel2.add(startTunnelButton,
                new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        buttonPanel2.add(stopTunnelButton,
                new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        pack();
    }

    public class StartSocksListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            updateConfiguration();

            socksServer = new NIOSocksServer(configuration);
            if (!socksServer.checkServerVersion()) {
                JOptionPane.showMessageDialog(MainFrame.this, "Sock to HTTP server is different version", "Version Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Start the socks server
            socksServer.start();
            stopSocksButton.setEnabled(true);
            startSocksButton.setEnabled(false);
        }
    }

    public class StopSocksListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            socksServer.stop();
            stopSocksButton.setEnabled(false);
            startSocksButton.setEnabled(true);
        }
    }

    public class AddTunnelListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            TunnelDialog dialog = new TunnelDialog(MainFrame.this);

            dialog.setVisible(true);

            if (dialog.getTunnel() != null) {
                tunnelTableModel.addTunnel(dialog.getTunnel());
                configuration.setTunnels(tunnelTableModel.getTunnels());
                configurationHolder.save();
            }
        }
    }

    public class RemoveTunnelListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int idx = tunnelTable.getSelectedRow();

            if (idx >= 0) {
                tunnelTableModel.removeTunnel(idx);
                configuration.setTunnels(tunnelTableModel.getTunnels());
                configurationHolder.save();
            }
        }
    }

    public class StartTunnelListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int idx = tunnelTable.getSelectedRow();

            if (idx >= 0) {
                tunnelTableModel.startTunnel(idx, configuration);
                if (tunnelTableModel.isActive(idx)) {
                    startTunnelButton.setEnabled(false);
                    stopTunnelButton.setEnabled(true);
                } else {
                    startTunnelButton.setEnabled(true);
                    stopTunnelButton.setEnabled(false);
                }
            }
        }
    }

    public class StopTunnelListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int idx = tunnelTable.getSelectedRow();

            if (idx >= 0) {
                tunnelTableModel.stopTunnel(idx);
                if (tunnelTableModel.isActive(idx)) {
                    startTunnelButton.setEnabled(false);
                    stopTunnelButton.setEnabled(true);
                } else {
                    startTunnelButton.setEnabled(true);
                    stopTunnelButton.setEnabled(false);
                }
            }
        }
    }

    public class TunnelTableListener implements ListSelectionListener {

        /**
         * @param e Description of the Parameter
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) {
            int idx = tunnelTable.getSelectedRow();

            if (idx < 0) {
                startTunnelButton.setEnabled(false);
                stopTunnelButton.setEnabled(false);
                removeTunnelButton.setEnabled(false);
            } else {
                removeTunnelButton.setEnabled(true);

                if (tunnelTableModel.isActive(idx)) {
                    startTunnelButton.setEnabled(false);
                    stopTunnelButton.setEnabled(true);
                } else {
                    startTunnelButton.setEnabled(true);
                    stopTunnelButton.setEnabled(false);
                }
            }
        }
    }

    public class HttpProxyListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            HttpProxyDialog dialog = new HttpProxyDialog(MainFrame.this, configurationHolder, configuration);

            dialog.setVisible(true);
        }

    }

    public class WindowCloseListener extends WindowAdapter {

        /**
         * @param e Description of the Parameter
         * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
         */
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    private class LocalNetworksListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalNetworksDialog dialog = new LocalNetworksDialog(MainFrame.this, configuration);

            dialog.setVisible(true);
        }
    }
}
