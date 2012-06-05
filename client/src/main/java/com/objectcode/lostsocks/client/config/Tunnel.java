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

// Title :        Tunnel.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Tunnel local port -> remote host:port

package com.objectcode.lostsocks.client.config;

public class Tunnel
{
  private int localPort;
  private String destinationUri;

  public Tunnel(int localPort, String destinationUri)
  {
    super();

    this.localPort = localPort;
    this.destinationUri = destinationUri;
  }

  public String getDestinationUri()
  {
    return destinationUri;
  }
  public int getLocalPort()
  {
    return localPort;
  }
}
