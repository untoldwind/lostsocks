package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.IConfigurationHolder;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class HttpProxyDialog extends JDialog {
    private static final long serialVersionUID = -5937050272427064956L;

    private IConfigurationHolder configurationHolder;
    private IConfiguration configuration;
    private JCheckBox enableProxyField;
    private JTextField proxyHostField;
    private JTextField proxyPortField;
    private JCheckBox proxyNeedAuthField;
    private JTextField proxyUserField;
    private JPasswordField proxyPasswordField;

    public HttpProxyDialog(JFrame owner, IConfigurationHolder configurationHolder, IConfiguration configuration) {
        super(owner, "Tunnel", true);

        this.configurationHolder = configurationHolder;
        this.configuration = configuration;
        enableProxyField = new JCheckBox();
        enableProxyField.setSelected(this.configuration.isUseProxy());
        enableProxyField.addItemListener(new EnableProxyListener());
        proxyHostField = new JTextField(40);
        proxyHostField.setText(this.configuration.getProxyHost());
        proxyHostField.setEnabled(this.configuration.isUseProxy());
        proxyPortField = new JTextField(5);
        proxyPortField.setText(this.configuration.getProxyPort());
        proxyPortField.setEnabled(this.configuration.isUseProxy());
        proxyNeedAuthField = new JCheckBox();
        proxyNeedAuthField.setSelected(this.configuration.isProxyNeedsAuthentication());
        proxyNeedAuthField.setEnabled(this.configuration.isUseProxy());
        proxyNeedAuthField.addItemListener(new EnableProxyListener());
        proxyUserField = new JTextField(20);
        proxyUserField.setText(this.configuration.getProxyUser());
        proxyUserField.setEnabled(this.configuration.isUseProxy() && this.configuration.isProxyNeedsAuthentication());
        proxyPasswordField = new JPasswordField(20);
        proxyPasswordField.setText(this.configuration.getProxyPassword());
        proxyPasswordField.setEnabled(this.configuration.isUseProxy() && this.configuration.isProxyNeedsAuthentication());

        initComponents();
    }

    protected void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());

        getContentPane().add(root, BorderLayout.CENTER);

        root.add(new JLabel("Proxy enabled:"),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(enableProxyField,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Proxy Host:"),
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(proxyHostField,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Proxy Port:"),
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(proxyPortField,
                new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Proxy need authentcation:"),
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(proxyNeedAuthField,
                new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Proxy Username:"),
                new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(proxyUserField,
                new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Proxy Password:"),
                new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));
        root.add(proxyPasswordField,
                new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        root.add(buttonPanel,
                new GridBagConstraints(0, 6, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        JButton okButton = new JButton("Ok");

        okButton.addActionListener(new OkButtonListener());

        buttonPanel.add(okButton,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(new CancelButtonListener());

        buttonPanel.add(cancelButton,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        pack();
    }

    public class OkButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            configuration.setUseProxy(enableProxyField.isSelected());
            configuration.setProxyHost(proxyHostField.getText());
            configuration.setProxyPort(proxyPortField.getText());
            configuration.setProxyNeedsAuthentication(proxyNeedAuthField.isSelected());
            configuration.setProxyUser(proxyUserField.getText());
            configuration.setProxyPassword(new String(proxyPasswordField.getPassword()));

            configurationHolder.save();

            setVisible(false);
        }
    }

    public class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

    public class EnableProxyListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            proxyHostField.setEnabled(enableProxyField.isSelected());
            proxyPortField.setEnabled(enableProxyField.isSelected());
            proxyNeedAuthField.setEnabled(enableProxyField.isSelected());
            proxyUserField.setEnabled(enableProxyField.isSelected() && proxyNeedAuthField.isSelected());
            proxyPasswordField.setEnabled(enableProxyField.isSelected() && proxyNeedAuthField.isSelected());
        }
    }
}
