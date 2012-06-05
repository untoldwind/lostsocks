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

// Title :        PropertiesFileReader.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Class to read a .properties file

package com.objectcode.lostsocks.common.util;

import java.util.*;

public class PropertiesFileReader
{

  public PropertiesFileReader()
  {
    super();
  }

  public static String getPropertyStringValue(String file, String property)
  {
    String ret = "";
    PropertyResourceBundle bundle = null;

    try
    {
      bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(file);
      ret = bundle.getString(property);
    }
    catch (Exception e)
    {
      // The properties file does not exist or the key does not exist
    }

    return (ret);
  }

  public static long getPropertyLongValue(String file, String property)
  {
    long ret = 0;

    try
    {
      ret = Long.parseLong(getPropertyStringValue(file, property));
    }
    catch(Exception e){}

    return(ret);
  }
}
