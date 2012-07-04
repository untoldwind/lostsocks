package com.objectcode.lostsocks.client.test;

import com.objectcode.lostsocks.client.config.PasswordEncoder;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PasswordEncoderTest {

    @Test
    public void testSimple() throws Exception {
        String encoded = PasswordEncoder.encodePassword("admin");
        String decoded = PasswordEncoder.decodePassword(encoded);

        assertThat(decoded).isEqualTo("admin");
    }
}
