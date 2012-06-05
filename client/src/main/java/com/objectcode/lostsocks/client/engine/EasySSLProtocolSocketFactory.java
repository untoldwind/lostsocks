/*
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package com.objectcode.lostsocks.client.engine;

import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * <p>
 * EasySSLProtocolSocketFactory can be used to creats SSL {@link Socket}s 
 * that accept self-signed certificates. 
 * </p>
 * <p>
 * This socket factory SHOULD NOT be used for productive systems 
 * due to security reasons, unless it is a concious decision and 
 * you are perfectly aware of security implications of accepting 
 * self-signed certificates
 * </p>
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * DISCLAIMER: HttpClient developers DO NOT actively support this component.
 * The component is provided as a reference material, which may be inappropriate
 * to be used without additional customization.
 */

public class EasySSLProtocolSocketFactory {

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(EasySSLProtocolSocketFactory.class);

    /**
     * Constructor for EasySSLProtocolSocketFactory.
     * 
     * Code sample:
     *  
     *     <blockquote>
     *     Protocol easyhttps = new Protocol( 
     *         "https", new EasySSLProtocolSocketFactory(), 443);
     *
     *     HttpClient client = new HttpClient();
     *     client.getHostConfiguration().setHost("localhost", 443, easyhttps);
     *     </blockquote>
     */
    public EasySSLProtocolSocketFactory() {
        super();
    }

    private static SSLSocketFactory getEasySSLSocketFactory() {
        SSLContext context = null;
        try {
            context = SSLContext.getInstance("SSL");
            context.init(
              null, 
              new TrustManager[] {new EasyX509TrustManager(null)}, 
              null);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.toString());
        }
        return context.getSocketFactory();
    }


    public Socket createSocket(
        String host,
        int port,
        InetAddress clientHost,
        int clientPort)
        throws IOException, UnknownHostException {

        Socket socket = getEasySSLSocketFactory().createSocket(
            host,
            port,
            clientHost,
            clientPort
        );
        return socket;
    }

    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
        return getEasySSLSocketFactory().createSocket(
            host,
            port
        );
    }

    public Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose)
        throws IOException, UnknownHostException {
        return getEasySSLSocketFactory().createSocket(
            socket,
            host,
            port,
            autoClose
        );
    }
}
