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
import com.objectcode.lostsocks.client.net.GenericSocksHandler;
import com.objectcode.lostsocks.common.Constants;
import com.objectcode.lostsocks.common.net.Connection;
import com.objectcode.lostsocks.common.net.DataPacket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;

/**
 * Description of the Class
 *
 * @author junglas
 * @created 1. Juni 2004
 */
public class ThreadCommunication extends Thread {
    private final static Log log = LogFactory.getLog(ThreadCommunication.class);

    private Connection source = null;

    private String id_conn = null;

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
            CompressedPacket connectionCreateResult = sendHttpMessage(configuration, RequestType.CONNECTION_CREATE, connectionCreate);

            if (connectionCreateResult != null ) {
                String data[] = connectionCreateResult.getDataAsString().split(":");
                id_conn = data[0];
                initOk = true;
                log.info("<SERVER> Connection created : " + id_conn);

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
        DataPacket dataPacket = new DataPacket();
        dataPacket.type = Constants.CONNECTION_CREATE;
        dataPacket.id = configuration.getUser() + ":" + configuration.getPassword() + ":" + configuration.getTimeout();
        dataPacket.tab = destinationUri.getBytes();

        // Send the connection
        int type = Constants.CONNECTION_UNSPECIFIED_TYPE;
        String serverInfoMessage = null;
        try {
            log.info("<CLIENT> SERVER, create a connection to " + destinationUri);
            DataPacket response = sendHttpMessage(configuration, dataPacket);
            type = response.type;
            id_conn = response.id;
            serverInfoMessage = new String(response.tab);
        } catch (Exception e) {
            log.error("<CLIENT> Cannot initiate a dialog with SERVER. Exception : " + e);
            return;
        }

        if (type == Constants.CONNECTION_CREATE_OK) {
            initOk = true;
            log.info("<SERVER> Connection created : " + id_conn);
        } else {
            log.error("<SERVER> " + serverInfoMessage);
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
                    DataPacket dataPacket = new DataPacket();
                    dataPacket.id = id_conn;

                    if (line == null) {
                        // Connection closed
                        log.info("<CLIENT> Application closed the connection");
                        log.info("<CLIENT> SERVER, close the connection " + id_conn);
                        requestOnlyIfClientActivity = false;
                        // Speeds up the shutdown
                        dataPacket.type = Constants.CONNECTION_DESTROY;
                        dataPacket.tab = Constants.TAB_EMPTY;
                    } else {
                        //configuration.printlnDebug("<CLIENT> SERVER, update the connection " + id_conn);
                        dataPacket.type = Constants.CONNECTION_REQUEST;
                        dataPacket.tab = line;
                    }

                    // Send the message
                    boolean packetTransmitted = false;
                    int retry = 0;
                    DataPacket response = null;
                    while ((!packetTransmitted) && (retry < 1 + configuration.getMaxRetries())) {
                        try {
                            response = sendHttpMessage(configuration, dataPacket);
                            packetTransmitted = true;
                        } catch (Exception e) {
                            retry++;
                            log.warn("<CLIENT> Cannot reach SERVER (try #" + retry + "). Exception : " + e);
                            Thread.sleep(configuration.getDelayBetweenTries());
                        }
                    }
                    if (retry == 1 + configuration.getMaxRetries()) {
                        log.error("<CLIENT> The maximum number of retries has been done");
                        log.error("<CLIENT> Disconnecting application");
                        source.disconnect();
                        dialogInProgress = false;
                        return;
                    }

                    // Write the received bytes
                    switch (response.type) {
                        case Constants.CONNECTION_RESPONSE:
                            source.write(response.tab);
                            break;
                        case Constants.CONNECTION_NOT_FOUND:
                            log.error("<SERVER> Connection not found : " + id_conn);
                            break;
                        case Constants.CONNECTION_DESTROY_OK:
                            log.info("<SERVER> As CLIENT asked, connection closed : " + id_conn);
                            break;
                        default:
                            log.warn("<CLIENT> SERVER sent an unexpected response type : " + response.type);
                            break;
                    }

                    // If the connection has been closed
                    if (response.isConnClosed) {
                        // Log
                        log.info("<SERVER> Remote server closed the connection : " + response.id);

                        // Close the source connection
                        log.info("<CLIENT> Disconnecting application");
                        source.disconnect();

                        // Stop the thread
                        dialogInProgress = false;
                    }

                    if (response.type == Constants.CONNECTION_DESTROY_OK) {
                        // Close the source connection
                        log.info("<CLIENT> Disconnecting application");
                        source.disconnect();

                        // Stop the thread
                        dialogInProgress = false;
                    }

                    if (response.type == Constants.CONNECTION_NOT_FOUND) {
                        // Close the source connection
                        log.error("<CLIENT> Disconnecting application");
                        source.disconnect();

                        // Stop the thread
                        dialogInProgress = false;
                    }
                }

                // Sleep
                //configuration.printlnDebug("<CLIENT> Sleeping " + configuration.getDelay() + " ms");
                Thread.sleep(configuration.getDelay());
            } catch (Exception e) {
                log.error("<CLIENT> Unexpected Exception : " + e);
            }
        }
    }

    public static CompressedPacket sendHttpMessage(IConfiguration config, RequestType requestType, CompressedPacket input) {
        HttpClient client = config.createHttpClient();

        HttpPost request = new HttpPost(config.getTargetPath() + requestType.getUri());

        request.setHeader("Content-Type", "application/x-compressed-bytes");
        request.setEntity(input.toEntity());

        try {
            HttpResponse response = client.execute(config.getTargetHost(), request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                log.error("Failed request " + request + " Status: " + response.getStatusLine());

                return null;
            }

            return CompressedPacket.fromEntity(response.getEntity());
        } catch (IOException e) {
            log.error("IOException " + e, e);
            return null;
        }
    }

    /**
     * Description of the Method
     *
     * @param config Description of the Parameter
     * @param source Description of the Parameter
     * @return Description of the Return Value
     * @throws IOException            Description of the Exception
     * @throws ClassNotFoundException Description of the Exception
     */
    public static DataPacket sendHttpMessage(IConfiguration config, DataPacket source)
            throws IOException, ClassNotFoundException {
        // Send an HTTP message
        DataPacket ret = null;
        InputStream is = null;
        ObjectInputStream ois = null;

        HttpClient client = config.createHttpClient();

        HttpEntityEnclosingRequestBase method = null;

        try {
            if (source.type == Constants.CONNECTION_CREATE || source.type == Constants.CONNECTION_VERSION_REQUEST) {
                method = new HttpPost("/connections");
            } else {
                method = new HttpPut("/connections/" + source.id);
            }

            method.setHeader("Content-Type", "application/x-java-serialized-object");

            // Write the serialized object as post data
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            java.util.zip.GZIPOutputStream zos = new java.util.zip.GZIPOutputStream(bos);
            ObjectOutputStream out = new ObjectOutputStream(zos);
            out.writeObject(source);
            out.flush();
            out.close();

            method.setEntity(new ByteArrayEntity(bos.toByteArray()));

            HttpResponse response = null;
            response = client.execute(config.getTargetHost(), method);

            // Create the InputStream
            is = response.getEntity().getContent();

            // Create the GZIPInputStream
            GZIPInputStream zis = new GZIPInputStream(is);

            // Create the ObjectInputStream
            ois = new ObjectInputStream(zis);

            // Read the response
            ret = (DataPacket) ois.readObject();

            // Close the stream
            ois.close();
            is.close();

            // Return the value
            return (ret);
        } finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }
}
