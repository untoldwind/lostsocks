package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;
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

/**
 * @author    junglas
 * @created   28. Mai 2004
 */
public class MainFrame extends JFrame
{
  private static final long serialVersionUID = -5199207523089060681L;

    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);

  private final static  String                     PROPERTIES_FILE       = "net.thetorn.stoh.client.init";

  private               JTextField                 m_serverUrlField;
  private               JTextField                 m_userField;
  private               JPasswordField             m_passwordField;
  private               JButton                    m_startSocksButton;
  private               JButton                    m_stopSocksButton;
  private               JButton                    m_addTunnelButton;
  private               JButton                    m_removeTunnelButton;
  private               JButton                    m_startTunnelButton;
  private               JButton                    m_stopTunnelButton;
  private               JButton                    m_httpProxyButton;
  private               JTable                     m_tunnelTable;
  private               TunnelsTableModel          m_tunnelTableModel;

  private NIOSocksServer m_socksServer;

  private               IConfiguration             m_configuration;


  /**
   *Constructor for the MainFrame object
   *
   * @param configuration  Description of the Parameter
   */
  public MainFrame( IConfiguration configuration )
  {
    super( "Socks to HTTP" );

    addWindowListener( new WindowCloseListener() );

    m_configuration = configuration;

    m_serverUrlField = new JTextField( m_configuration.getUrlString() );
    m_userField = new JTextField( m_configuration.getUser() );
    m_passwordField = new JPasswordField( m_configuration.getPassword() );
    m_startSocksButton = new JButton( "Start Socks" );
    m_startSocksButton.addActionListener( new StartSocksListener() );
    m_stopSocksButton = new JButton( "Stop Socks" );
    m_stopSocksButton.setEnabled( false );
    m_stopSocksButton.addActionListener( new StopSocksListener() );
    m_httpProxyButton = new JButton( "HTTP Proxy" );
    m_httpProxyButton.addActionListener( new HttpProxyListener() );
    m_addTunnelButton = new JButton( "Add Tunnel" );
    m_addTunnelButton.addActionListener( new AddTunnelListener() );
    m_removeTunnelButton = new JButton( "Remove Tunnel" );
    m_removeTunnelButton.addActionListener( new RemoveTunnelListener() );
    m_removeTunnelButton.setEnabled( false );
    m_startTunnelButton = new JButton( "Start Tunnel" );
    m_startTunnelButton.addActionListener( new StartTunnelListener() );
    m_startTunnelButton.setEnabled( false );
    m_stopTunnelButton = new JButton( "Stop Tunnel" );
    m_stopTunnelButton.addActionListener( new StopTunnelListener() );
    m_stopTunnelButton.setEnabled( false );
    m_tunnelTableModel = new TunnelsTableModel( m_configuration.getTunnels() );
    m_tunnelTable = new JTable( m_tunnelTableModel );
    m_tunnelTable.getSelectionModel().addListSelectionListener( new TunnelTableListener() );

    initComponents();

    // Proxy on
    if ( configuration.isUseProxy() ) {
      // Logging
      log.info( "Using proxy " + configuration.getProxyHost() + ":" + configuration.getProxyPort() );

      // JDK 1.3 and below
      System.getProperties().put( "proxySet", "true" );
      System.getProperties().put( "proxyHost", configuration.getProxyHost() );
      System.getProperties().put( "proxyPort", configuration.getProxyPort() );

      // JDK 1.4
      System.getProperties().put( "http.proxyHost", configuration.getProxyHost() );
      System.getProperties().put( "http.proxyPort", configuration.getProxyPort() );
    } else {
      // JDK 1.3 and below
      System.getProperties().remove( "proxySet" );
      System.getProperties().remove( "proxyHost" );
      System.getProperties().remove( "proxyPort" );

      // JDK 1.4
      System.getProperties().remove( "http.proxyHost" );
      System.getProperties().remove( "http.proxyPort" );
    }
  }


  /**
   * Description of the Method
   */
  void updateConfiguration()
  {
    m_configuration.setUrlString( m_serverUrlField.getText() );
    m_configuration.setUser( m_userField.getText() );
    m_configuration.setPassword( new String( m_passwordField.getPassword() ) );

    m_configuration.save();
  }


  /**
   * Description of the Method
   */
  protected void initComponents()
  {
    JPanel       root          = new JPanel( new GridBagLayout() );

    getContentPane().add( root, BorderLayout.CENTER );

    root.add( new JLabel( "URL:" ),
        new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_serverUrlField,
        new GridBagConstraints( 1, 0, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "User:" ),
        new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_userField,
        new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "Password:" ),
        new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_passwordField,
        new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    root.add( m_httpProxyButton,
        new GridBagConstraints( 2, 1, 1, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    
    JPanel       buttonPanel   = new JPanel( new GridBagLayout() );

    root.add( buttonPanel,
        new GridBagConstraints( 0, 3, 3, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    buttonPanel.add( m_startSocksButton,
        new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    buttonPanel.add( m_stopSocksButton,
        new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    JScrollPane  scrollPane    = new JScrollPane( m_tunnelTable );

    root.add( scrollPane,
        new GridBagConstraints( 0, 4, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    JPanel       buttonPanel2  = new JPanel( new GridBagLayout() );

    root.add( buttonPanel2,
        new GridBagConstraints( 0, 5, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    buttonPanel2.add( m_addTunnelButton,
        new GridBagConstraints( 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    buttonPanel2.add( m_removeTunnelButton,
        new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    buttonPanel2.add( m_startTunnelButton,
        new GridBagConstraints( 2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    buttonPanel2.add( m_stopTunnelButton,
        new GridBagConstraints( 3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    pack();
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class StartSocksListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      updateConfiguration();

      m_socksServer = new NIOSocksServer( m_configuration );
      if ( !m_socksServer.checkServerVersion() ) {
        JOptionPane.showMessageDialog( MainFrame.this, "Sock to HTTP server is different version", "Version Error", JOptionPane.ERROR_MESSAGE );
        return;
      }

      // Start the socks server
      m_socksServer.start();
      m_stopSocksButton.setEnabled( true );
      m_startSocksButton.setEnabled( false );
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class StopSocksListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      m_socksServer.stop();
      m_stopSocksButton.setEnabled( false );
      m_startSocksButton.setEnabled( true );
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class AddTunnelListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      TunnelDialog  dialog  = new TunnelDialog( MainFrame.this );

      dialog.setVisible( true );

      if ( dialog.getTunnel() != null ) {
        m_tunnelTableModel.addTunnel( dialog.getTunnel() );
        m_configuration.setTunnels( m_tunnelTableModel.getTunnels() );
        m_configuration.save();
      }
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class RemoveTunnelListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      int  idx  = m_tunnelTable.getSelectedRow();

      if ( idx >= 0 ) {
        m_tunnelTableModel.removeTunnel(idx);
        m_configuration.setTunnels( m_tunnelTableModel.getTunnels() );
        m_configuration.save();
      }
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class StartTunnelListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      int  idx  = m_tunnelTable.getSelectedRow();

      if ( idx >= 0 ) {
        m_tunnelTableModel.startTunnel(idx, m_configuration);
        if ( m_tunnelTableModel.isActive( idx ) ) {
          m_startTunnelButton.setEnabled( false );
          m_stopTunnelButton.setEnabled( true );
        } else {
          m_startTunnelButton.setEnabled( true );
          m_stopTunnelButton.setEnabled( false );
        }
      }
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class StopTunnelListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      int  idx  = m_tunnelTable.getSelectedRow();

      if ( idx >= 0 ) {
        m_tunnelTableModel.stopTunnel(idx);
        if ( m_tunnelTableModel.isActive( idx ) ) {
          m_startTunnelButton.setEnabled( false );
          m_stopTunnelButton.setEnabled( true );
        } else {
          m_startTunnelButton.setEnabled( true );
          m_stopTunnelButton.setEnabled( false );
        }
      }
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class TunnelTableListener implements ListSelectionListener
  {

    /**
     * @param e  Description of the Parameter
     * @see      javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged( ListSelectionEvent e )
    {
      int  idx  = m_tunnelTable.getSelectedRow();

      if ( idx < 0 ) {
        m_startTunnelButton.setEnabled( false );
        m_stopTunnelButton.setEnabled( false );
        m_removeTunnelButton.setEnabled( false );
      } else {
        m_removeTunnelButton.setEnabled( true );

        if ( m_tunnelTableModel.isActive( idx ) ) {
          m_startTunnelButton.setEnabled( false );
          m_stopTunnelButton.setEnabled( true );
        } else {
          m_startTunnelButton.setEnabled( true );
          m_stopTunnelButton.setEnabled( false );
        }
      }
    }
  }

  public class HttpProxyListener implements ActionListener
  {

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      HttpProxyDialog dialog = new HttpProxyDialog(MainFrame.this, m_configuration);
      
      dialog.setVisible(true);
    }
    
  }

  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class WindowCloseListener extends WindowAdapter
  {

    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    public void windowClosing( WindowEvent e )
    {
      System.exit( 0 );
    }
  }
}
