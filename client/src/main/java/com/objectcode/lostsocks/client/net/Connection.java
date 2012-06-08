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

// Title :        Connection.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  TCP/IP Connection

package com.objectcode.lostsocks.client.net;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Connection
{
  public static final int CONNECTION_SERVER_TYPE = 1;
  public static final int CONNECTION_CLIENT_TYPE = 2;

  public static final int SO_TIMEOUT = 100;
  public static final int BUFFER_SIZE = 65536;  // 64KB

  // Members
  private int m_type = 0;
  private Socket m_socket = null;
  private ServerSocket m_serversocket = null;
  private String m_host = null;
  private int m_port = -1;
  private byte[] m_buffer = new byte[BUFFER_SIZE];

  private InputStream m_inputstream = null;
  private OutputStream m_outputstream = null;
  private BufferedInputStream m_bufferedinputstream = null;
  private BufferedOutputStream m_bufferedoutputstream = null;

  private boolean connected = false;

  // Constructor
  public Connection(int type)
  {
    m_type = type;
  }

  // Constructor for a server connection
  public Connection(Socket socket)
  {
    m_type = CONNECTION_SERVER_TYPE;
    m_socket = socket;

    try
    {
      m_outputstream = m_socket.getOutputStream();
      m_inputstream = m_socket.getInputStream();
      m_bufferedoutputstream = new BufferedOutputStream(m_outputstream);
      m_bufferedinputstream = new BufferedInputStream(m_inputstream);
    }
    catch (IOException e)
    {
      return;
    }

    try
    {
      m_socket.setSoTimeout(SO_TIMEOUT);
    }
    catch (Exception e){}

    connected = true;
  }

  // Connection
  public int connect(String host, int port)
  {
    if (connected) return(0);

    m_host = host;
    m_port = port;

    if (m_type == CONNECTION_SERVER_TYPE)
    {
      //System.out.println("Server connection started...");
      try
      {
        m_serversocket = new ServerSocket(m_port);
        m_socket = m_serversocket.accept();
      }
      catch (IOException e)
      {
        return(-1);
      }
    }
    else
    {
      //System.out.println("Client connection started...");
      try
      {
        m_socket = new Socket(m_host, m_port);
      }
      catch (IOException e)
      {
        return(-1);
      }
    }

    try
    {
      m_outputstream = m_socket.getOutputStream();
      m_inputstream = m_socket.getInputStream();
      m_bufferedoutputstream = new BufferedOutputStream(m_outputstream);
      m_bufferedinputstream = new BufferedInputStream(m_inputstream);
    }
    catch (IOException e)
    {
      return(-1);
    }

    try
    {
      m_socket.setSoTimeout(SO_TIMEOUT);
    }
    catch (Exception e){}

    connected = true;
    return(0);
  }

  // Disconnection
  public synchronized int disconnect()
  {
    if (!connected) return(0);

    try
    {
      m_bufferedinputstream.close();
      m_bufferedoutputstream.flush();
      m_bufferedoutputstream.close();
    }
    catch (IOException e){}

    try
    {
      m_socket.close();
    }
    catch (IOException e)
    {
      return(-1);
    }

    connected = false;
    return(0);
  }

  public boolean isConnected()
  {
    return(connected);
  }

  public byte[] read()
  {
    if (!connected) return(null);

    int len = -1;
    try
    {
      len = m_bufferedinputstream.read(m_buffer, 0, BUFFER_SIZE);
    }
    catch (InterruptedIOException e)
    {
      len = 0;
    }
    catch (IOException e){}

    if (len == -1)
    {
      //System.out.println("The connection has been closed");
      return(null);
    }

    byte[] ret = new byte[len];
    for (int i = 0; i < len; i++) ret[i] = m_buffer[i];
    return(ret);
  }

  public int write(byte[] toWrite)
  {
    if (toWrite == null) return(0);
    if (toWrite.length == 0) return(0);

    if (!connected) return(0);

    try
    {
      m_bufferedoutputstream.write(toWrite);
      m_bufferedoutputstream.flush();
    }
    catch (IOException e)
    {
      return(-1);
    }
    return(0);
  }

  public Socket getSocket()
  {
    return(m_socket);
  }
}
