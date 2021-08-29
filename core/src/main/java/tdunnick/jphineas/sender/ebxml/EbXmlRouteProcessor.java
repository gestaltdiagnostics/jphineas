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

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.ebxml.EbXmlRequest;
import tdunnick.jphineas.logging.Log;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.mime.MimeReceiver;
import tdunnick.jphineas.sender.RouteProcessor;
import tdunnick.jphineas.util.SocketFactory;
import tdunnick.jphineas.xml.ResponseXml;
import tdunnick.jphineas.xml.SoapXml;

/**
 * This is the route processor for ebXML (standard PHINMS) outgoing messages. It
 * gets called from a QueueThread when a message is found to send. It uses the
 * Route's configuration, along with queue information to package the payload,
 * make a connection, and send the message. It updates the queue based on the
 * response (or lack of).
 * 
 * @author user
 *
 */
public class EbXmlRouteProcessor extends RouteProcessor {
	private RouteConfig config = null;

	public boolean configure(RouteConfig cfg) {
		if ((this.config = cfg) == null)
			return false;
		return true;
	}

	/**
	 * The ebXML processor is responsible for sending an ebXML request, opening the
	 * appropriate connection including any needed authentication, and processing
	 * the response.
	 * 
	 * @param req the message to send
	 * @return ResponseXml if successful
	 * @see tdunnick.jphineas.sender.RouteProcessor#process(tdunnick.jphineas.queue.PhineasQRow)
	 */
	public ResponseXml process(RouteConfig config, EbXmlRequest ebReq) {
		ResponseXml xml = null;

		// get a soap request for this row
		SoapXml soap = ebReq.getSoapRequest(config);
		// check for basic authentication
		String authid = null;
		String pw = config.getAuthenticationType();
		if ((pw != null) && (pw.equalsIgnoreCase("basic"))) {
			authid = config.getAuthenticationId();
			pw = config.getAuthenticationPassword();
		}
		// build a request string...
		String req = "POST " + config.getProtocol() + "://" + config.getHost() + ":" + config.getPort()
				+ config.getPath() + " HTTP/1.1\r\n";

		// now ready to send it off
		Socket socket = null;
		try {
			MimeContent mime = null;
			// send all chunks over the same connection
			while ((mime = ebReq.getMessagePackage(soap)) != null) {
				// set up a connection
				if (socket == null) {
					if ((socket = SocketFactory.createSocket(config)) == null) {
						Log.error("Failed to open connection to " + config.getHost());
						break;
					}
				}
				OutputStream out = socket.getOutputStream();
				InputStream in = socket.getInputStream();
				// insert BASIC authentication
				if (authid != null) {
					mime.setBasicAuth(authid, pw);
				}
				Log.debug("sending EbXml request:\n" + req + mime.toString());
				out.write(req.getBytes());
				out.write(mime.toString().getBytes());
				out.flush();
				Log.debug("waiting for reply");
				MimeContent msg = MimeReceiver.receive(in);
				// if the remote closed the socket, then clean up
				if (!socket.isConnected() || true) {
					socket.close();
					socket = null;
				}
				// then parse the reply and update our row
				Log.debug("response:\n" + msg.toString());
				xml = ebReq.ParseMessagePackage(msg, soap.getHdrMessageId());
				if (xml == null || !xml.ok()) {
					break;
				}
			}
		} catch (Exception e) {
			Log.error("Failed sending EbXML message", e);
		} finally {
			// TODO digest authentication response?
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return xml;
	}
}
