package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.Tunnel;
import com.objectcode.lostsocks.client.engine.NIOGenericServer;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author    junglas
 * @created   28. Mai 2004
 */
public class TunnelsTableModel extends AbstractTableModel
{
  private static final long serialVersionUID = -483811386797964583L;

  List  m_tunnels;


  /**
   *Constructor for the TunnelsTableModel object
   *
   * @param tunnels  Description of the Parameter
   */
  public TunnelsTableModel( Tunnel[] tunnels )
  {
    m_tunnels = new ArrayList();

    int  i;

    for ( i = 0; i < tunnels.length; i++ ) {
      m_tunnels.add( new TunnelElement( tunnels[i] ) );
    }
  }


  /**
   * @param column  Description of the Parameter
   * @return        The columnName value
   * @see           javax.swing.table.TableModel#getColumnName(int)
   */
  public String getColumnName( int column )
  {
    switch ( column ) {
      case 0:
        return "Local Port";
      case 1:
        return "Destination";
      case 2:
        return "Status";
    }
    return "";
  }


  /**
   * @return   The columnCount value
   * @see      javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 3;
  }


  /**
   * @return   The rowCount value
   * @see      javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    return m_tunnels.size();
  }


  /**
   * @param rowIndex     Description of the Parameter
   * @param columnIndex  Description of the Parameter
   * @return             The valueAt value
   * @see                javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    if ( rowIndex < 0 || rowIndex >= m_tunnels.size() ) {
      return "";
    }

    TunnelElement  element  = ( TunnelElement ) m_tunnels.get( rowIndex );

    switch ( columnIndex ) {
      case 0:
        return new Integer( element.getTunnel().getLocalPort() );
      case 1:
        return element.getTunnel().getDestinationUri();
      case 2:
        return element.getTunnelServer() != null ? "Active" : "Inactive";
    }

    return "";
  }


  /**
   * Gets the active attribute of the TunnelsTableModel object
   *
   * @param rowIndex  Description of the Parameter
   * @return          The active value
   */
  public boolean isActive( int rowIndex )
  {
    if ( rowIndex < 0 || rowIndex >= m_tunnels.size() ) {
      return false;
    }

    TunnelElement  element  = ( TunnelElement ) m_tunnels.get( rowIndex );

    return element.getTunnelServer() != null;
  }


  /**
   * Gets the tunnels attribute of the TunnelsTableModel object
   *
   * @return   The tunnels value
   */
  public Tunnel[] getTunnels()
  {
    Tunnel  tunnels[]  = new Tunnel[m_tunnels.size()];
    int     i;

    for ( i = 0; i < tunnels.length; i++ ) {
      tunnels[i] = ( ( TunnelElement ) m_tunnels.get( i ) ).getTunnel();
    }

    return tunnels;
  }


  /**
   * Adds a feature to the Tunnel attribute of the TunnelsTableModel object
   *
   * @param tunnel  The feature to be added to the Tunnel attribute
   */
  public void addTunnel( Tunnel tunnel )
  {
    m_tunnels.add( new TunnelElement( tunnel ) );
    fireTableDataChanged();
  }


  /**
   * Description of the Method
   *
   * @param rowIndex  Description of the Parameter
   */
  public void removeTunnel( int rowIndex )
  {
    if ( rowIndex < 0 || rowIndex >= m_tunnels.size() ) {
      return;
    }

    TunnelElement  element  = ( TunnelElement ) m_tunnels.get( rowIndex );

    element.stop();

    m_tunnels.remove( rowIndex );
    fireTableDataChanged();
  }


  /**
   * Description of the Method
   *
   * @param rowIndex       Description of the Parameter
   * @param configuration  Description of the Parameter
   */
  public void startTunnel( int rowIndex, IConfiguration configuration )
  {
    if ( rowIndex < 0 || rowIndex >= m_tunnels.size() ) {
      return;
    }

    TunnelElement  element  = ( TunnelElement ) m_tunnels.get( rowIndex );

    element.start( configuration );

    fireTableRowsUpdated( rowIndex, rowIndex );
  }


  /**
   * Description of the Method
   *
   * @param rowIndex  Description of the Parameter
   */
  public void stopTunnel( int rowIndex )
  {
    if ( rowIndex < 0 || rowIndex >= m_tunnels.size() ) {
      return;
    }

    TunnelElement  element  = ( TunnelElement ) m_tunnels.get( rowIndex );

    element.stop();

    fireTableRowsUpdated( rowIndex, rowIndex );
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  private static class TunnelElement
  {
    Tunnel                   m_tunnel;
    NIOGenericServer m_tunnelServer;


    /**
     *Constructor for the TunnelElement object
     *
     * @param tunnel  Description of the Parameter
     */
    TunnelElement( Tunnel tunnel )
    {
      m_tunnel = tunnel;
    }


    /**
     * @return   Returns the tunnel.
     */
    public Tunnel getTunnel()
    {
      return m_tunnel;
    }


    /**
     * @return   Returns the tunnelServer.
     */
    public NIOGenericServer getTunnelServer()
    {
      return m_tunnelServer;
    }


    /**
     * Description of the Method
     *
     * @param configuration  Description of the Parameter
     */
    public void start( IConfiguration configuration )
    {
      if ( m_tunnel.getDestinationUri().length() > 0 ) {
        m_tunnelServer = new NIOGenericServer( m_tunnel, configuration );
        m_tunnelServer.start();
      }
    }


    /**
     * Description of the Method
     */
    public void stop()
    {
      if ( m_tunnelServer != null ) {
        m_tunnelServer.stop();
        m_tunnelServer = null;
      }
    }
  }
}
