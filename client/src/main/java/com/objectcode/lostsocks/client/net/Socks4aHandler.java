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

// Title :        Socks4aHandler.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Socks v4a Handler

package com.objectcode.lostsocks.client.net;

import com.objectcode.lostsocks.client.config.IConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Socks4aHandler extends Socks4Handler
{
  private static final Log log = LogFactory.getLog(Socks4aHandler.class);
  
  public Socks4aHandler(IConfiguration configuration, byte[] tab, Connection conn)
  {
    super(configuration, tab, conn);
    label = "Socks4a Handler";
  }

  public boolean isHandled()
  {
    if ((version == 4) && (tab[4] == 0) && (tab[5] == 0) && (tab[6] == 0))
    {
      if (command != SOCKS4_BIND_COMMAND)
      {
        destPort = 256 * b2i(tab[2]) + b2i(tab[3]);
        destIP = "0.0.0." + b2i(tab[7]);
        int pos = -1;
        for (int i = 8; i < tab.length; i++)
        {
          if (tab[i] == 0) pos = i;
        }
        userId = new String(tab, 8, pos - 8);
        dnsName = new String(tab, pos + 1, tab.length - pos - 1);
        return(true);
      }
      else
      {
        // Socks4a BIND Command not supported yet
        log.warn("Socks4a BIND command not supported yet");
      }
    }
    return(false);
  }
}
