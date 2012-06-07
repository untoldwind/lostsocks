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

// Title :        DataPacket.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Data to transfer between the client part & the server part

package net.thetorn.stoh.common.net;

import com.objectcode.lostsocks.common.Constants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class DataPacket implements java.io.Serializable
{
  public int type = Constants.CONNECTION_UNSPECIFIED_TYPE;
  public String id = "";
  public byte[] tab = Constants.TAB_EMPTY;
  public boolean isConnClosed = false;
  
  protected void writeObject ( ObjectOutputStream out )
  	throws IOException
  {
    out.writeInt(type);
    out.writeUTF(id);
    out.writeBoolean(isConnClosed);
    out.writeInt(tab.length);
    out.write(tab);
  }

  protected void readObject ( ObjectInputStream in )
  	throws IOException, ClassNotFoundException
  {
    type = in.readInt();
    id = in.readUTF();
    isConnClosed = in.readBoolean();
    int tabLength = in.readInt();
    tab = new byte[tabLength];
    in.readFully(tab);
  }
}
