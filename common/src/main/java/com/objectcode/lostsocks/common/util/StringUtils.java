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

// Title :        StringUtils.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Class to manipulate strings

package com.objectcode.lostsocks.common.util;

import java.util.*;

public class StringUtils
{

  public StringUtils()
  {
    super();
  }

  // Split a string
  public static String[] stringSplit(String string, String tokens, boolean trimStrings)
  {
    if (string == null) return(null);
    if (string.length() == 0) return(new String[0]);

    Vector res = new Vector();
    StringTokenizer stk = new StringTokenizer(string, tokens, false);
    while (stk.hasMoreTokens()) res.addElement(stk.nextToken());
    String[] res2 = new String[res.size()];
    for (int i=0;i<res.size();i++)
    {
      res2[i]=(String)res.elementAt(i);
      if (trimStrings) res2[i] = res2[i].trim();
    }
    return(res2);
  }
}
