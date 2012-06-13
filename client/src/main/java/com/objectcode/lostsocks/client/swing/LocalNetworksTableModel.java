package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.Network;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class LocalNetworksTableModel extends AbstractTableModel {
    private final List<Network> localNetworks;

    public LocalNetworksTableModel(List<Network> localNetworks) {
        this.localNetworks = localNetworks;
    }

    @Override
    public int getRowCount() {
        return localNetworks.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName( int column )
    {
        switch ( column ) {
            case 0:
                return "Network";
            case 1:
                return "Netmask";
        }
        return "";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ( rowIndex <0 || rowIndex >= localNetworks.size())
            return null;

        Network network = localNetworks.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return network.getNetworkString();
            case 1:
                return network.getMaskString();
        }
        return null;
    }
}
