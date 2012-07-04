package com.objectcode.lostsocks.client.test;

import com.objectcode.lostsocks.client.config.SimpleWildcard;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SimpleWildcardTest {
    @Test
    public void testForIP4() throws Exception {
        SimpleWildcard loopback = new SimpleWildcard("127.0.0.1");

        assertThat(loopback.matches("127.0.0.1")).isTrue();
        assertThat(loopback.matches("127.0.0.2")).isFalse();
        assertThat(loopback.matches("127.0.0.1.123")).isFalse();
        assertThat(loopback.matches("localhost")).isFalse();

        SimpleWildcard classALocal = new SimpleWildcard("192.168.100.*");

        assertThat(classALocal.matches("192.168.100.97")).isTrue();
        assertThat(classALocal.matches("192.168.100")).isFalse();
        assertThat(classALocal.matches("192.168.100.")).isFalse();
        assertThat(classALocal.matches("192.168.1.97")).isFalse();

        SimpleWildcard classBLocal = new SimpleWildcard("172.16.*.*");

        assertThat(classBLocal.matches("172.16.12.13")).isTrue();
        assertThat(classBLocal.matches("172.16.100.200")).isTrue();
        assertThat(classBLocal.matches("172.17.12.13")).isFalse();
        assertThat(classBLocal.matches("172.16")).isFalse();
        assertThat(classBLocal.matches("172.16.")).isFalse();
        assertThat(classBLocal.matches("172.16.12.")).isFalse();
    }

    @Test
    public void testForHostname() throws Exception {
        SimpleWildcard localhost = new SimpleWildcard("localhost");

        assertThat(localhost.matches("localhost")).isTrue();
        assertThat(localhost.matches("localhost.localdomain")).isFalse();
        assertThat(localhost.matches("some.where.to")).isFalse();

        SimpleWildcard someDomain = new SimpleWildcard("*.somedomain.to");

        assertThat(someDomain.matches("something.somedomain.to")).isTrue();
        assertThat(someDomain.matches("something.somedomain")).isFalse();
        assertThat(someDomain.matches(".somedomain.to")).isFalse();
        assertThat(someDomain.matches("somedomain.to")).isFalse();
    }
}
