package com.objectcode.lostsocks.client.config;

import java.util.regex.Pattern;

public class SimpleWildcard {
    private final String wildcard;
    private final Pattern pattern;

    public SimpleWildcard(String wildcard) {
        this.wildcard = wildcard;

        String regex = "^" + wildcard.replace(".", "\\.").replace("*", "[^\\.]+") + "$";

        pattern = Pattern.compile(regex);
    }

    public String getWildcard() {
        return wildcard;
    }

    public boolean matches(String value) {
        return pattern.matcher(value).matches();
    }
}
