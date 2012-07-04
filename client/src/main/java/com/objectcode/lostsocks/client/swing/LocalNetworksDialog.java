package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.IConfigurationHolder;
import com.objectcode.lostsocks.client.config.SimpleWildcard;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LocalNetworksDialog extends JDialog {
    IConfigurationHolder configurationHolder;
    LocalNetworksTableModel localNetworksTableModel;
    JTable localNetworksTable;
    JTextField wildcardField = new JTextField();
    JButton addButton = new JButton("Add");
    JButton removeButton = new JButton("Remove");

    public LocalNetworksDialog(JFrame owner, IConfigurationHolder configurationHolder) {
        super(owner, "Local networks", true);

        this.configurationHolder = configurationHolder;
        IConfiguration configuration = configurationHolder.getActiveConfiguration();

        localNetworksTableModel = new LocalNetworksTableModel(configuration);
        localNetworksTable = new JTable(localNetworksTableModel);
        initComponents();
    }

    protected void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());

        getContentPane().add(root, BorderLayout.CENTER);

        JScrollPane scrollPane = new JScrollPane(localNetworksTable);

        root.add(scrollPane,
                new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        root.add(wildcardField,
                new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        addButton.setEnabled(false);
        root.add(addButton,
                new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        removeButton.setEnabled(false);
        root.add(removeButton,
                new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        root.add(buttonPanel,
                new GridBagConstraints(0, 2, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 0, 0));

        JButton okButton = new JButton("Ok");

        okButton.addActionListener(new OkButtonListener());

        buttonPanel.add(okButton,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        JButton cancelButton = new JButton("Cancel");

        cancelButton.addActionListener(new CancelButtonListener());

        buttonPanel.add(cancelButton,
                new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

        localNetworksTable.getSelectionModel().addListSelectionListener(new NetworkSelectionListener());
        wildcardField.addCaretListener(new WildcardCaretListener());
        addButton.addActionListener(new AddButtonListener());
        removeButton.addActionListener(new RemoveButtonListener());
        pack();
    }

    public class OkButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            configurationHolder.save();
            setVisible(false);
        }
    }

    public class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

    private class NetworkSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int rowIndex = localNetworksTable.getSelectedRow();

            SimpleWildcard row = localNetworksTableModel.getRow(rowIndex);

            wildcardField.setText(row.getWildcard());
            addButton.setEnabled(false);
            removeButton.setEnabled(true);
        }
    }

    private class WildcardCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            String value = wildcardField.getText();

            addButton.setEnabled(value.length() > 0);
            removeButton.setEnabled(false);
            localNetworksTable.clearSelection();
        }
    }

    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            localNetworksTableModel.addRow(new SimpleWildcard(wildcardField.getText()));

            wildcardField.setText("");
            addButton.setEnabled(false);
            removeButton.setEnabled(false);
            localNetworksTable.clearSelection();
        }
    }

    private class RemoveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int rowIndex = localNetworksTable.getSelectedRow();

            localNetworksTableModel.removeRow(rowIndex);

            wildcardField.setText("");
            addButton.setEnabled(false);
            removeButton.setEnabled(false);
            localNetworksTable.clearSelection();
        }
    }
}
