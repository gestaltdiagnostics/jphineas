/*
 *  Copyright (c) 2015-2016 Thomas Dunnick (https://mywebspace.wisc.edu/tdunnick/web)
 *  
 *  This file is part of jPhineas
 *
 *  jPhineas is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  jPhineas is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with jPhineas.  If not, see <http://www.gnu.org/licenses/>.
 */

package tdunnick.jphineas.sender.ebxml;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

import org.apache.log4j.Logger;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.ebxml.EbXmlRequest;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.mime.MimeReceiver;
import tdunnick.jphineas.util.SocketFactory;
import tdunnick.jphineas.xml.ResponseXml;
import tdunnick.jphineas.xml.SoapXml;

/**
 * This is the connection for sending ebXML (standard PHINMS) messages.
 * It uses the Route's configuration to make a connection, and send the message.
 * 
 * @author user
 *
 */
public class EbXmlRouteConnection {
	private RouteConfig config = null;
	private Socket socket = null;
	private String basicAuthid = null;
	private String basicPassword;

	private static final Logger LOG = Logger.getLogger(EbXmlRouteConnection.class);

	public EbXmlRouteConnection(RouteConfig cfg) {
		Objects.requireNonNull(cfg, "Route configuration cannot be null");
		this.config = cfg;
	}

	public void open() {
		if(socket == null) {
			socket = SocketFactory.createSocket(config);
			if (socket == null) {
				LOG.error("Failed to open connection to " + config.getHost());
			}
		}
		
		// check for basic authentication
		basicPassword = config.getAuthenticationType();
		if ((basicPassword != null) && (basicPassword.equalsIgnoreCase("basic"))) {
			basicAuthid = config.getAuthenticationId();
			basicPassword = config.getAuthenticationPassword();
		}
	}

	public void close() {
		// if the remote closed the socket, then clean up
		if (socket != null) {
			try {
				socket.close();
			}
			catch(Exception e) {
				// ignore
			}
			socket = null;
		}
	}

	public boolean isValid() {
		boolean valid = false;
		if(socket != null) {
			valid = !(socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown());
		}
		
		return valid;
	}

	/**
	 * Sends the given message
	 * 
	 * @param req the message to send
	 * @return ResponseXml if successful
	 */
	public ResponseXml send(EbXmlRequest ebReq) {
		ebReq.setRouteConfig(config);
		ResponseXml xml = null;

		// get a soap request for this row
		SoapXml soap = ebReq.getSoapRequest();
		// build a request string...
		String req = "POST " + config.getProtocol() + "://" + config.getHost() + ":" + config.getPort()
				+ config.getPath() + " HTTP/1.1\r\n";

		try {
			MimeContent mime = null;
			// send all chunks over the same connection
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			while ((mime = ebReq.getMessagePackage(soap)) != null) {
				// insert BASIC authentication
				if (basicAuthid != null) {
					mime.setBasicAuth(basicAuthid, basicPassword);
				}
				LOG.debug("sending EbXml request:\n" + req + mime.toString());
				out.write(req.getBytes());
				out.write(mime.toString().getBytes());
				out.flush();
				LOG.debug("waiting for reply");
				MimeContent msg = MimeReceiver.receive(in);
				// then parse the reply and update our row
				LOG.debug("response:\n" + msg.toString());
				xml = ebReq.ParseMessagePackage(msg, soap.getHdrMessageId());
				if (xml == null || !xml.ok()) {
					break;
				}
			}
		} catch (Exception e) {
			LOG.error("Failed sending EbXML message", e);
		}
		return xml;
	}
}
