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

// Title :        Socks5Handler.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Socks v5 Handler

package com.objectcode.lostsocks.client.net;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.common.net.Connection;

public class Socks5Handler extends GenericSocksHandler
{
  public final static int SOCKS5_NO_AUTHENTICATION_REQUIRED = 0;
  public final static int SOCKS5_NO_ACCEPTABLE_METHODS = 255;
  // Socks commands
  public final static int SOCKS5_CONNECT = 1;
  // Address types
  public final static int SOCKS5_IPV4 = 1;
  public final static int SOCKS5_DOMAIN = 3;
  // Responses
  public final static int SOCKS5_OK = 0;
  public final static int SOCKS5_KO = 1;

  protected Connection conn;

  public Socks5Handler(IConfiguration configuration, byte[] tab, Connection conn)
  {
    super(configuration, tab, conn);
    this.conn = conn;

    label = "Socks5 Handler";
    version = b2i(tab[0]);
  }

  public boolean isHandled()
  {
    int numMethods = b2i(tab[1]);
    boolean methodAccepted = false;

    if ((version == 5) && (numMethods > 0))
    {
      byte[] handshake = new byte[2];
      int i = 0;
      while ((i < numMethods) && (i < tab.length) && (!methodAccepted))
      {
        methodAccepted = (b2i(tab[i + 2]) == SOCKS5_NO_AUTHENTICATION_REQUIRED);
        i++;
      }
      handshake[0] = 5; // version
      if (methodAccepted)
      {
        handshake[1] = i2b(SOCKS5_NO_AUTHENTICATION_REQUIRED);
        conn.write(handshake);
        byte[] request = conn.read();
        version = b2i(request[0]);
        command = b2i(request[1]);
        int adressType = b2i(request[3]);
        if ((version == 5) && (command == SOCKS5_CONNECT) && ((adressType == SOCKS5_DOMAIN) || (adressType == SOCKS5_IPV4)))
        {
          if (adressType == SOCKS5_IPV4)
          {
            destPort = 256 * b2i(request[8]) + b2i(request[9]);
            destIP = "" + b2i(request[4]) + "." + b2i(request[5]) + "." + b2i(request[6]) + "." + b2i(request[7]);
          }
          else
          {
            int len = b2i(request[4]);
            destIP = "0.0.0.0";
            dnsName = new String(request, 5, len);
            destPort = 256 * b2i(request[len+5]) + b2i(request[len+6]);
          }
          return(true);
        }
      }
      else
      {
        handshake[1] = i2b(SOCKS5_NO_ACCEPTABLE_METHODS);
        conn.write(handshake);
      }
    }
    return(false);
  }

  public byte[] buildResponse(int responseType)
  {
    byte[] ret = new byte[10];

    String[] bytes = destIP.split("\\.");
    ret[0] = 5;
    ret[1] = (byte)(responseType == RESPONSE_SUCCESS ? SOCKS5_OK : SOCKS5_KO);
    ret[2] = 0;
    ret[3] = SOCKS5_IPV4;
    ret[4] = i2b(Integer.parseInt(bytes[0]));
    ret[5] = i2b(Integer.parseInt(bytes[1]));
    ret[6] = i2b(Integer.parseInt(bytes[2]));
    ret[7] = i2b(Integer.parseInt(bytes[3]));
    ret[8] = i2b(destPort / 256);
    ret[9] = i2b(destPort % 256);
    return (ret);
  }
}
