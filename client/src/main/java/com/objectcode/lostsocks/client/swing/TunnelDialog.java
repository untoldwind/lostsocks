package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.Tunnel;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author junglas
 * @created 28. Mai 2004
 */
public class TunnelDialog extends JDialog {
    private static final long serialVersionUID = -2948030769789211603L;

    private JTextField m_localPortField;

    private JTextField m_destinationHostField;

    private JTextField m_destinationPortField;

    private Tunnel m_tunnel;

    /**
     * Constructor for the TunnelDialog object
     *
     * @param owner Description of the Parameter
     */
    public TunnelDialog(JFrame owner) {

        super(owner, "Tunnel", true);

        m_localPortField = new JTextField(10);
        m_destinationHostField = new JTextField(20);
        m_destinationPortField = new JTextField(10);

        initComponents();
    }

    /**
     * @return Returns the tunnel.
     */
    public Tunnel getTunnel() {

        return m_tunnel;
    }

    /**
     * Description of the Method
     */
    protected void initComponents() {

        JPanel root = new JPanel(new GridBagLayout());

        getContentPane().add(root, BorderLayout.CENTER);

        root.add(new JLabel("Local Port:"),
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
        root.add(m_localPortField,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Destination Host:"),
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
        root.add(m_destinationHostField,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
        root.add(new JLabel("Destination Host:"),
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));
        root.add(m_destinationPortField,
                new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        root.add(buttonPanel,
                new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton okButton = new JButton("Ok");

        okButton.addActionListener(new OkButtonListener());

        buttonPanel.add(okButton,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(new CancelButtonListener());

        buttonPanel.add(cancelButton,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));

        pack();
    }

    /**
     * Description of the Class
     *
     * @author junglas
     * @created 28. Mai 2004
     */
    public class OkButtonListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {

            m_tunnel = new Tunnel(Integer.parseInt(m_localPortField.getText()),
                    m_destinationHostField.getText() + ":" + m_destinationPortField.getText());

            setVisible(false);
        }
    }

    /**
     * Description of the Class
     *
     * @author junglas
     * @created 28. Mai 2004
     */
    public class CancelButtonListener implements ActionListener {
        /**
         * @param e Description of the Parameter
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {

            setVisible(false);
        }
    }
}
