package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.SimpleWildcard;

import javax.swing.table.AbstractTableModel;

public class LocalNetworksTableModel extends AbstractTableModel {
    IConfiguration configuration;

    public LocalNetworksTableModel(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int getRowCount() {
        return configuration.getLocalNetworks().size();
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName( int column )
    {
        switch ( column ) {
            case 0:
                return "Network";
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ( rowIndex <0 || rowIndex >= configuration.getLocalNetworks().size())
            return null;

        SimpleWildcard network = configuration.getLocalNetworks().get(rowIndex);

        switch (columnIndex) {
            case 0:
                return network.getWildcard();
        }
        return null;
    }

    public SimpleWildcard getRow(int rowIndex) {
        return configuration.getLocalNetworks().get(rowIndex);
    }

    public void addRow(SimpleWildcard wildcard) {
        configuration.addLocalNetwork(wildcard);

        fireTableDataChanged();
    }

    public void removeRow(int rowIndex) {
        configuration.removeLocalNetwork(rowIndex);

        fireTableDataChanged();
    }
}
