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

// Title :        Socks4Handler.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Socks v4 Handler

package com.objectcode.lostsocks.client.net;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.common.net.Connection;
import com.objectcode.lostsocks.common.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Socks4Handler extends GenericSocksHandler
{
  private final static Log log = LogFactory.getLog ( Socks4Handler.class );
  
  public static final int SOCKS4_REPLY_VERSION = 0; // Must be 0
  public static final int SOCKS4_OK = 90; // request granted
  public static final int SOCKS4_KO = 91; // request rejected or failed
  public static final int SOCKS4_IDENTD_KO = 92; // request rejected because SOCKS server cannot connect to identd on the client
  public static final int SOCKS4_USERID_KO = 93; // request rejected because the client program and identd report different user-ids

  public static final int SOCKS4_BIND_COMMAND = 2;  // BIND command code (not supported yet)

  public Socks4Handler(IConfiguration configuration, byte[] tab, Connection conn)
  {
    super(configuration, tab, conn);

    label = "Socks4 Handler";
    version = b2i(tab[0]);
    command = b2i(tab[1]);
  }

  public boolean isHandled()
  {
    if (version == 4)
    {
      // Strict basic Socks4
      if ((tab[4] != 0) || (tab[5] != 0) || (tab[6] != 0))
      {
        if (command != SOCKS4_BIND_COMMAND)
        {
          destPort = 256 * b2i(tab[2]) + b2i(tab[3]);
          destIP = "" + b2i(tab[4]) + "." + b2i(tab[5]) + "." + b2i(tab[6]) + "." + b2i(tab[7]);
          userId = new String(tab, 8, tab.length - 8 - 1);
          return(true);
        }
        else
        {
          // Socks4 BIND Command not supported yet
          log.warn("Socks4 BIND command not supported yet");
        }
      }
    }
    return(false);
  }

  public byte[] buildResponse(int responseType)
  {
    byte[] ret = new byte[8];

    String[] bytes = StringUtils.stringSplit(destIP, ".", false);
    ret[0] = (byte)SOCKS4_REPLY_VERSION;
    ret[1] = (byte)(responseType == RESPONSE_SUCCESS ? SOCKS4_OK : SOCKS4_KO);
    ret[2] = i2b(destPort / 256);
    ret[3] = i2b(destPort % 256);
    ret[4] = i2b(Integer.parseInt(bytes[0]));
    ret[5] = i2b(Integer.parseInt(bytes[1]));
    ret[6] = i2b(Integer.parseInt(bytes[2]));
    ret[7] = i2b(Integer.parseInt(bytes[3]));

    return (ret);
  }
}
