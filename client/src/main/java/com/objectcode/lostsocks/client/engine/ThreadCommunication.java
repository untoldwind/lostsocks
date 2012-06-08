/*
 *  This file is part of Socks via HTTP.
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  Socks via HTTP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with Socks via HTTP; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
// Title :        ThreadCommunication.java
// Version :      1.0
// Copyright :    Copyright (c) 2001-2002
// Author :       Florent CUETO  & Sebastien LEBRETON <socksviahttp@cqs.dyndns.org>
// Description :  Communication between socks via HTTP client & the servlet (HTTP Tunneling)

package com.objectcode.lostsocks.client.engine;

import com.objectcode.lostsocks.client.config.IConfiguration;
import com.objectcode.lostsocks.client.net.Connection;
import com.objectcode.lostsocks.client.net.GenericSocksHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;

import java.io.IOException;

/**
 * Description of the Class
 *
 * @author junglas
 * @created 1. Juni 2004
 */
public class ThreadCommunication extends Thread {
    private final static Log log = LogFactory.getLog(ThreadCommunication.class);

    private Connection source = null;

    private String connectionId = null;

    private String destinationUri = null;

    private boolean initOk = false;

    private IConfiguration configuration = null;

    private boolean requestOnlyIfClientActivity = false;

    /**
     * Constructor for the ThreadCommunication object
     *
     * @param source        Description of the Parameter
     * @param configuration Description of the Parameter
     */
    public ThreadCommunication(Connection source, IConfiguration configuration) {

        super();
        this.source = source;
        this.configuration = configuration;
        //this.requestOnlyIfClientActivity = configuration.isRequestOnlyIfClientActivity();
        this.requestOnlyIfClientActivity = false;

        // Get the destination
        GenericSocksHandler socksHandler = null;
        try {
            socksHandler = GenericSocksHandler.getHandler(this.configuration, this.source);
        } catch (Exception e) {
            log.warn("<CLIENT> No valid Socks handler found");
            return;
        }

        destinationUri =
                (socksHandler.getDnsName() != null ? socksHandler.getDnsName() : socksHandler.getDestIP()) + ":"
                        + socksHandler.getDestPort();
        log.info("<CLIENT> An application asked a connection to " + destinationUri);
        log.debug("<CLIENT> Handler type : " + socksHandler.getLabel());

        CompressedPacket connectionCreate = new CompressedPacket(destinationUri + ":" + configuration.getTimeout(), false);

        try {
            log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
            CompressedPacket connectionCreateResult = sendHttpMessage(configuration, RequestType.CONNECTION_CREATE, null, connectionCreate);

            if (connectionCreateResult != null) {
                String data[] = connectionCreateResult.getDataAsString().split(":");
                connectionId = data[0];
                initOk = true;
                log.info("<SERVER> Connection created : " + connectionId);

                // Send the response packet to the socks client
                GenericSocksHandler replyPacket = socksHandler;
                replyPacket.setDestIP(data[1]);
                replyPacket.setDestPort(Integer.parseInt(data[2]));
                this.source.write(replyPacket.buildResponse(GenericSocksHandler.RESPONSE_SUCCESS));
            } else {
                log.error("<SERVER> Connection creation failed");

                // Send the response packet to the socks client
                GenericSocksHandler replyPacket = socksHandler;
                this.source.write(replyPacket.buildResponse(GenericSocksHandler.RESPONSE_FAILURE));
            }
        } catch (Exception e) {
            log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + e);
            return;
        }
    }

    /**
     * Constructor for the ThreadCommunication object
     *
     * @param source         Description of the Parameter
     * @param destinationUri Description of the Parameter
     * @param configuration  Description of the Parameter
     */
    public ThreadCommunication(Connection source, String destinationUri, IConfiguration configuration) {

        super();
        this.source = source;
        this.destinationUri = destinationUri;
        this.configuration = configuration;

        log.info("<CLIENT> An application asked a connection to " + destinationUri);

        // Create a connection on the servlet server
        CompressedPacket connectionCreate = new CompressedPacket(destinationUri + ":" + configuration.getTimeout(), false);

        // Send the connection
        String serverInfoMessage = null;
        try {
            log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
            CompressedPacket connectionCreateResult = sendHttpMessage(configuration, RequestType.CONNECTION_CREATE, null, connectionCreate);
            if (connectionCreateResult != null) {
                String data[] = connectionCreateResult.getDataAsString().split(":");
                connectionId = data[0];
                initOk = true;
                log.info("<SERVER> Connection created : " + connectionId);
            } else {
                log.error("<SERVER> Connection creation failed");
            }
        } catch (Exception e) {
            log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + e);
            return;
        }
    }

    // Main task

    /**
     * Main processing method for the ThreadCommunication object
     */
    public void run() {

        if (!initOk) {
            log.error("<CLIENT> Disconnecting application");
            source.disconnect();
            return;
        }

        boolean dialogInProgress = true;
        byte[] line;
        long initialTime = new java.util.Date().getTime();
        long lastUpdateTime = initialTime;

        while (dialogInProgress == true) {
            try {
                line = source.read();

                long now = new java.util.Date().getTime();

                // Check if we have to start minimizing HTTP traffic
                if (now - initialTime > configuration.getDontTryToMinimizeTrafficBefore()) {
                    requestOnlyIfClientActivity = configuration.isRequestOnlyIfClientActivity();
                }

                boolean forceRequest = (now > configuration.getForceRequestAfter() + lastUpdateTime);
                if (configuration.getForceRequestAfter() == 0) {
                    forceRequest = false;
                }
                if ((!requestOnlyIfClientActivity) || (forceRequest) || (line == null) || (line.length > 0)) {
                    lastUpdateTime = new java.util.Date().getTime();

                    if (line == null) {
                        // Connection closed
                        log.info("<CLIENT> Application closed the connection");
                        log.info("<CLIENT> SERVER, close the connection " + connectionId);
                        requestOnlyIfClientActivity = false;

                        CompressedPacket connectionCloseResult = sendHttpMessage(configuration, RequestType.CONNECTION_CLOSE, connectionId, null);

                        if ( connectionCloseResult == null ) {
                            log.error("<CLIENT> SERVER fail closing");
                        }
                        log.info("<CLIENT> Disconnecting application (regular)");
                        source.disconnect();
                        dialogInProgress = false;
                    } else {
                        CompressedPacket connectionRequset = new CompressedPacket(line, false);
                        CompressedPacket connectionResult = sendHttpMessage(configuration, RequestType.CONNECTION_REQUEST, connectionId, connectionRequset);

                        if (connectionResult == null) {
                            log.error("<CLIENT> Disconnecting application");
                            source.disconnect();
                            dialogInProgress = false;
                            return;
                        }

                        source.write(connectionResult.getData());

                        if (connectionResult.isEndOfCommunication()) {
                            // Log
                            log.info("<SERVER> Remote server closed the connection : " + connectionId);

                            // Close the source connection
                            log.info("<CLIENT> Disconnecting application (regular)");
                            source.disconnect();

                            // Stop the thread
                            dialogInProgress = false;
                        }
                    }
                }

                // Sleep
                //configuration.printlnDebug("<CLIENT> Sleeping " + configuration.getDelay() + " ms");
                Thread.sleep(configuration.getDelay());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("<CLIENT> Unexpected Exception : " + e, e);
            }
        }
    }

    public static CompressedPacket sendHttpMessage(IConfiguration config, RequestType requestType, String connectionId, CompressedPacket input) {
        HttpClient client = config.createHttpClient();

        HttpRequest request = requestType.getHttpRequest(config.getTargetPath(), connectionId, input != null ? input.toEntity(): null);

        for (int retry = 0; retry <= config.getMaxRetries(); retry++) {
            try {
                HttpResponse response = client.execute(config.getTargetHost(), request);

                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    return CompressedPacket.fromEntity(response.getEntity());
                }
                log.error("Failed request (try #" + retry + ") " + request + " Status: " + response.getStatusLine());
            } catch (IOException e) {
                log.error("IOException (try #" + retry + ") " + e, e);
                return null;
            }
        }
        log.error("<CLIENT> The maximum number of retries has been done");
        return null;
    }
}
