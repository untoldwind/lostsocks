package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LocalNetworksDialog extends JDialog {
    LocalNetworksTableModel localNetworksTableModel;
    JTable localNetworksTable;

    public LocalNetworksDialog( JFrame owner, IConfiguration configuration )
    {
        super( owner, "Local networks", true );

        localNetworksTableModel = new LocalNetworksTableModel(configuration.getLocalNetworks());
        localNetworksTable = new JTable(localNetworksTableModel);
        initComponents();
    }

    protected void initComponents()
    {
        JPanel   root          = new JPanel( new GridBagLayout() );

        getContentPane().add( root, BorderLayout.CENTER );

        JScrollPane scrollPane = new JScrollPane(localNetworksTable);

        root.add(scrollPane,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));


        JPanel   buttonPanel   = new JPanel( new GridBagLayout() );

        root.add( buttonPanel,
                new GridBagConstraints( 0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

        JButton  okButton      = new JButton( "Ok" );

        okButton.addActionListener(new OkButtonListener());

        buttonPanel.add( okButton,
                new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

        JButton  cancelButton  = new JButton( "Cancel" );

        cancelButton.addActionListener(new CancelButtonListener());

        buttonPanel.add( cancelButton,
                new GridBagConstraints( 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

        pack();
    }

    public class OkButtonListener implements ActionListener
    {
        /**
         * @param e  Description of the Parameter
         * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed( ActionEvent e )
        {
            setVisible( false );
        }
    }

    public class CancelButtonListener implements ActionListener
    {
        public void actionPerformed( ActionEvent e )
        {
            setVisible( false );
        }
    }
}
