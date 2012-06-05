package com.objectcode.lostsocks.client.swing;

import com.objectcode.lostsocks.client.config.IConfiguration;

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

public class HttpProxyDialog extends JDialog
{
  private static final long serialVersionUID = -5937050272427064956L;
  
  private IConfiguration m_configuration;
  private JCheckBox      m_enableProxyField;
  private JTextField     m_proxyHostField;
  private JTextField     m_proxyPortField;
  private JCheckBox      m_proxyNeedAuthField;
  private JTextField     m_proxyUserField;
  private JPasswordField m_proxyPasswordField;
  
  public HttpProxyDialog( JFrame owner, IConfiguration configuration )
  {
    super( owner, "Tunnel", true );

    m_configuration = configuration;
    m_enableProxyField = new JCheckBox();
    m_enableProxyField.setSelected(m_configuration.isUseProxy());
    m_enableProxyField.addItemListener(new EnableProxyListener());
    m_proxyHostField = new JTextField(40);
    m_proxyHostField.setText(m_configuration.getProxyHost());
    m_proxyHostField.setEnabled(m_configuration.isUseProxy());
    m_proxyPortField = new JTextField(5);
    m_proxyPortField.setText(m_configuration.getProxyPort());
    m_proxyPortField.setEnabled(m_configuration.isUseProxy());
    m_proxyNeedAuthField = new JCheckBox();
    m_proxyNeedAuthField.setSelected(m_configuration.isProxyNeedsAuthentication());
    m_proxyNeedAuthField.setEnabled(m_configuration.isUseProxy());
    m_proxyNeedAuthField.addItemListener(new EnableProxyListener());
    m_proxyUserField = new JTextField(20);
    m_proxyUserField.setText(m_configuration.getProxyUser());
    m_proxyUserField.setEnabled(m_configuration.isUseProxy() && m_configuration.isProxyNeedsAuthentication());
    m_proxyPasswordField = new JPasswordField(20);
    m_proxyPasswordField.setText(m_configuration.getProxyPassword());
    m_proxyPasswordField.setEnabled(m_configuration.isUseProxy() && m_configuration.isProxyNeedsAuthentication());

    initComponents();
  }
  
  protected void initComponents()
  {
    JPanel   root          = new JPanel( new GridBagLayout() );

    getContentPane().add( root, BorderLayout.CENTER );

    root.add( new JLabel( "Proxy enabled:"),
        new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_enableProxyField,
        new GridBagConstraints( 1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "Proxy Host:" ),
        new GridBagConstraints( 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_proxyHostField,
        new GridBagConstraints( 1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "Proxy Port:" ),
        new GridBagConstraints( 0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_proxyPortField,
        new GridBagConstraints( 1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "Proxy need authentcation:" ),
        new GridBagConstraints( 0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_proxyNeedAuthField,
        new GridBagConstraints( 1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "Proxy Username:" ),
        new GridBagConstraints( 0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_proxyUserField,
        new GridBagConstraints( 1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( new JLabel( "Proxy Password:" ),
        new GridBagConstraints( 0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );
    root.add( m_proxyPasswordField,
        new GridBagConstraints( 1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

    JPanel   buttonPanel   = new JPanel( new GridBagLayout() );

    root.add( buttonPanel,
        new GridBagConstraints( 0, 6, 2, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets( 2, 2, 2, 2 ), 0, 0 ) );

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


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class OkButtonListener implements ActionListener
  {
    /**
     * @param e  Description of the Parameter
     * @see      java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e )
    {
      m_configuration.setUseProxy(m_enableProxyField.isSelected());
      m_configuration.setProxyHost(m_proxyHostField.getText());
      m_configuration.setProxyPort(m_proxyPortField.getText());
      m_configuration.setProxyNeedsAuthentication(m_proxyNeedAuthField.isSelected());
      m_configuration.setProxyUser(m_proxyUserField.getText());
      m_configuration.setProxyPassword(new String(m_proxyPasswordField.getPassword()));
      
      m_configuration.save();
      
      setVisible( false );
    }
  }


  /**
   * Description of the Class
   *
   * @author    junglas
   * @created   28. Mai 2004
   */
  public class CancelButtonListener implements ActionListener
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
  
  public class EnableProxyListener implements ItemListener
  {
    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged( ItemEvent e )
    {
      m_proxyHostField.setEnabled(m_enableProxyField.isSelected());
      m_proxyPortField.setEnabled(m_enableProxyField.isSelected());
      m_proxyNeedAuthField.setEnabled(m_enableProxyField.isSelected());
      m_proxyUserField.setEnabled(m_enableProxyField.isSelected() && m_proxyNeedAuthField.isSelected());
      m_proxyPasswordField.setEnabled(m_enableProxyField.isSelected() && m_proxyNeedAuthField.isSelected());
    }
    
  }
}
