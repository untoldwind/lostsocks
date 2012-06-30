package com.objectcode.lostsocks.client.utils;

import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author junglas
 */
public class PropertiesHelper {
    public static long getLong(Properties properties, String key, long def) {
        String value = properties.getProperty(key);

        if (value == null)
            return def;

        return Long.parseLong(value);
    }

    public static int getInt(Properties properties, String key, int def) {
        String value = properties.getProperty(key);

        if (value == null)
            return def;

        return Integer.parseInt(value);
    }

    public static String getString(Properties properties, String key, String def) {
        String value = properties.getProperty(key);

        if (value == null)
            return def;

        return value;
    }

    public static String[] getStrings(Properties properties, String key, String[] def) {
        String value = properties.getProperty(key);

        if (value == null)
            return def;

        StringTokenizer t = new StringTokenizer(value, ",;");
        String values[] = new String[t.countTokens()];
        int i;

        for (i = 0; t.hasMoreTokens(); i++)
            values[i] = t.nextToken().trim();

        return values;
    }

    public static boolean getBoolean(Properties properties, String key, boolean def) {
        String value = properties.getProperty(key);

        if (value == null)
            return def;

        return Boolean.valueOf(value).booleanValue();
    }

    public static void setString(Properties properties, String key, String value) {
        if (value != null)
            properties.setProperty(key, value);
    }

    public static void setStrings(Properties properties, String key, String[] values) {
        if (values != null && values.length > 0 ) {
            StringBuffer value = new StringBuffer();

            for ( int i = 0; i < values.length; i++ ) {
                if ( i > 0 )
                    value.append(",");
                value.append(values[i]);
            }
            properties.setProperty(key, value.toString());
        }
    }
}
