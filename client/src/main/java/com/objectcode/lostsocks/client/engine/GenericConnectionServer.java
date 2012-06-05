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

// Title :        GenericConnectionServer.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Connection Server (Client part of socks via HTTP)

package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.config.Tunnel;
import com.objectcode.lostsocks.common.net.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class GenericConnectionServer extends Thread
{
  private static final Log log = LogFactory.getLog(GenericConnectionServer.class);
  
  public static final int LISTEN_TIMEOUT = 2000;
  private static final String LOCALHOST_IP = "127.0.0.1";

  private ServerSocket serverSocket = null;
  public boolean listening = true;
  private IConfiguration configuration = null;
  private Tunnel tunnel = null;

  public GenericConnectionServer(Tunnel tunnel, IConfiguration configuration)
  {
    super();
    this.tunnel = tunnel;
    this.configuration = configuration;
  }

  public void run()
  {
    // Let's start
    try
    {
      serverSocket = new ServerSocket(tunnel.getLocalPort());
      serverSocket.setSoTimeout(LISTEN_TIMEOUT);
    }
    catch (IOException e)
    {
      log.error("<CLIENT> Unexpected Exception while creating ServerSocket in GenericConnectionServer : ", e);
    }

    while(listening)
    {
      try
      {
        Socket s = serverSocket.accept();
        if ((!s.getInetAddress().getHostAddress().equals(LOCALHOST_IP)) && configuration.isListenOnlyLocalhost())
        {
          // Log
          log.error("<CLIENT> Incoming generic connection refused from IP " + s.getInetAddress().getHostAddress());

          // Close the socket
          s.close();
        }
        else
        {
          log.error("<CLIENT> Incoming generic connection accepted from IP " + s.getInetAddress().getHostAddress());
          Connection conn = new Connection(s);
          ThreadCommunication tc = new ThreadCommunication(conn, tunnel.getDestinationUri() , configuration);
          tc.start();
        }
      }
      catch (InterruptedIOException iioe){}
      catch (Exception e)
      {
        log.error("<CLIENT> Unexpected Exception while listening in GenericConnectionServer : ", e);
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
