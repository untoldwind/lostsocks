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

// Title :        GenericSocksHandler.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Generic Socks Handler

package com.objectcode.lostsocks.client.net;

import com.objectcode.lostsocks.client.config.IConfiguration;

public abstract class GenericSocksHandler
{
  public static final int RESPONSE_SUCCESS = 0;
  public static final int RESPONSE_FAILURE = -1;
  protected String label = "Generic Socks Handler";
  protected IConfiguration configuration;
  protected byte[] tab = null;

  protected int version;
  protected int command;
  protected int destPort;
  protected String destIP = null;
  protected String userId = null;
  protected String password = null;
  protected String dnsName = null;

  public GenericSocksHandler(IConfiguration configuration, byte[] tab, Connection conn)
  {
    super();
    this.configuration = configuration;
    this.tab = tab;
  }

  public abstract byte[] buildResponse(int ResponseType);
  public abstract boolean isHandled();

  public static GenericSocksHandler getHandler(IConfiguration config, Connection conn) throws Exception
  {
    GenericSocksHandler ret = null;
    byte[] data = conn.read();

    /*for (int i = 0; i < data.length; i++)
    {
      byte b = data[i];
      System.out.println("byte " + i + " : " + Byte.toString(b));
    }*/

    ret = new Socks4Handler(config, data, conn);
    if (!ret.isHandled())
    {
        ret = new Socks4aHandler(config, data, conn);
        if (!ret.isHandled())
        {
            ret = new Socks5Handler(config, data, conn);
            if (!ret.isHandled())
            {
                throw new Exception("No valid socks handler found");
            }
        }
    }
    return ret;
  }

  public int b2i(byte b)
  {
    return (b < 0 ? 256 + b : b);
  }

  public byte i2b(int i)
  {
    return (i > 127 ? (byte)(i - 256) : (byte)i);
  }

  public int getCommand()
  {
    return command;
  }

  public byte[] getTab()
  {
    return tab;
  }
  public String getDestIP()
  {
    return destIP;
  }
  public int getDestPort()
  {
    return destPort;
  }
  public String getDnsName()
  {
    return dnsName;
  }
  public void setDestIP(String destIP)
  {
    this.destIP = destIP;
  }
  public void setDestPort(int destPort)
  {
    this.destPort = destPort;
  }
  public int getVersion()
  {
    return version;
  }
  public String getUserId()
  {
    return userId;
  }
  public String getPassword()
  {
    return password;
  }
  public String getLabel()
  {
    return label;
  }
}
