/*
This file is part of Socks via HTTP.

This package is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

Socks via HTTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Socks via HTTP; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

// Title :        SocksConnectionServer.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Socks Server (Client part of socks via HTTP)

package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.common.Constants;
import com.objectcode.lostsocks.common.net.Connection;
import com.objectcode.lostsocks.common.net.DataPacket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocksConnectionServer extends Thread
{
  private final static Log log = LogFactory.getLog(SocksConnectionServer.class);
  
  public static final int LISTEN_TIMEOUT = 2000;
  private static final String LOCALHOST_IP = "127.0.0.1";

  private ServerSocket serverSocket = null;
  public boolean listening = true;
  private IConfiguration configuration = null;

  public SocksConnectionServer(IConfiguration configuration)
  {
    super();
    this.configuration = configuration;
  }

  public boolean checkServerVersion()
  {
    // Create a connection on the servlet server
    DataPacket dataPacket = new DataPacket();
    dataPacket.type = Constants.CONNECTION_VERSION_REQUEST;
    dataPacket.id = Constants.APPLICATION_VERSION;
    dataPacket.tab = "Version check".getBytes();

    // Send the connection
    int type = Constants.CONNECTION_UNSPECIFIED_TYPE;
    String id = null;
    String serverInfoMessage = null;
    try
    {
      log.info("<CLIENT> Version check : " + Constants.APPLICATION_VERSION + " - URL : " + configuration.getUrlString());
      DataPacket response = ThreadCommunication.sendHttpMessage(configuration, dataPacket);
      type = response.type;
      id = response.id;
    }
    catch(Exception e)
    {
      log.fatal("<CLIENT> Version check : Cannot check the server version. Exception : ", e);
      return(false);
    }

    // Check the version
    if (type == Constants.CONNECTION_VERSION_RESPONSE_KO)
    {
      log.fatal("<SERVER> Version not supported. Version needed : " + id);
      return(false);
    }
    if (type == Constants.CONNECTION_VERSION_RESPONSE_OK)
    {
      if (!Constants.APPLICATION_VERSION.equals(id)) log.warn("<SERVER> Version supported but you should use version " + id);
      else log.info("<SERVER> Version check : OK");
    }
    return(true);
  }

  public void run()
  {
    // Let's start
    try
    {
      serverSocket = new ServerSocket(configuration.getPort());
      serverSocket.setSoTimeout(LISTEN_TIMEOUT);
    }
    catch (IOException e)
    {
      log.error("<CLIENT> Unexpected Exception while creating ServerSocket in SocksConnectionServer : ", e);
    }

    while(listening)
    {
      try
      {
        Socket s = serverSocket.accept();
        if ((!s.getInetAddress().getHostAddress().equals(LOCALHOST_IP)) && configuration.isListenOnlyLocalhost())
        {
          // Log
          log.warn("<CLIENT> Incoming Socks connection refused from IP " + s.getInetAddress().getHostAddress());

          // Close the socket
          s.close();
        }
        else
        {
          log.info("<CLIENT> Incoming Socks connection accepted from IP " + s.getInetAddress().getHostAddress());
          Connection conn = new Connection(s);
          ThreadCommunication tc = new ThreadCommunication(conn, configuration);
          tc.start();
        }
      }
      catch (InterruptedIOException iioe){}
      catch (Exception e)
      {
        log.error("<CLIENT> Unexpected Exception while listening in SocksConnectionServer : ", e);
      }
    }

    try
    {
      // Close the ServerSocket
      serverSocket.close();
    }
    catch (IOException e){}
  }
  
  public void close()
  {
    listening = false;
  }
}
