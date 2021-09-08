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

package tdunnick.jphineas.receiver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import tdunnick.jphineas.common.JPhineas;
import tdunnick.jphineas.config.ReceiverConfig;
import tdunnick.jphineas.config.ServiceConfig;
import tdunnick.jphineas.mime.MimeContent;
import tdunnick.jphineas.mime.MimeReceiver;
import tdunnick.jphineas.xml.SoapXml;

/**
 * jPhineas Receiver servlet. This services HTTP (ebXML) protocol requests.
 * Separate receivers will be needed, either as independent threads started here,
 * or from a separate servlet to manage things like MLLP or sFTP.
 * Also, since this is a servlet, multi-threading is handled by the J2EE container
 * and we need only be concerned with thread safety (not thread management as was needed by the sender).
 * 
 * @author Thomas Dunnick
 * 
 */
public class Receiver extends HttpServlet
{
	/*
	 * identifiers found in our XML configuration file - note this follows
	 * receiver.xml conventions, with our own additions.
	 */
	/** properties */
	private static String configName = null;
	
	/** current status */
	private static String status = "stopped";
	
	/** location of CPA's */
	private static String cpadir = null;
	
	/** reply cache */
	private static ReplyCache replies = null;
	
	/** our stats */
	String[] heading = 
	  { "Date/Time", "File Name", "Status", "Error", "Response" };
	
	// processors accessed by service/action pairs
	private static HashMap <String, ReceiverProcessor> processors = 
		new HashMap <String, ReceiverProcessor>();
	
	private static final Logger LOG = Logger.getLogger(Receiver.class);

	/**
	 * Basic sanity checks on configuration, initialize security and queue
	 * manager, set configuration defaults, instantiate and load service 
	 * processors and map.
	 * 
	 * @return true if successful
	 */
	public static synchronized boolean startup ()
	{
		if (configName == null)
			return false;
		Thread.currentThread().setName("Receiver");
		
		// add bouncy castle to the security providers for missing algorithms
	  java.security.Security.addProvider (
	  		new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// get receiver's configuration
		ReceiverConfig config = new ReceiverConfig();
		if (!config.load(new File (configName)))
		{
			status = "failed loading " + configName;
			return false;
		}

		LOG.info("Receiver starting...");
		// CPA folder and reply cache
		cpadir = config.getCpaDirectory();
		replies = new ReplyCache (config.getCacheFolder());
		// set up our service/action processor map
		int n = config.getServiceCount ();
		while (n-- > 0)
		{
			ServiceConfig cfg = config.getService (n);
			String serviceAction = cfg.getService() + ":" + cfg.getAction();
			ReceiverProcessor processor = cfg.getProcessor ();
			if (processor == null)
			{
				LOG.error("Failed loading processor for " + serviceAction);
				continue;
			}
			if (!processor.configure (cfg))
			{
				LOG.error("Failed configuring processor for " + serviceAction);
				continue;
			}
			processors.put(serviceAction, processor);
		}
		LOG.info("Receiver started");
		status = "running";
		return (true);
	}

	public static boolean shutdown ()
	{
		processors.clear();
		status = "stopped";
		return true;
	}
	
	/**
	 * Provide some interactive feedback, generally for user connectivity testing of
	 * this server.
	 * @param req  from client
	 * @param resp to client
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		String msg = JPhineas.getHtml () + "<p>Receiver is " + status + "</p>";
		resp.getOutputStream().write (msg.getBytes());
	}

	/**
	 * Handles ebXML web service requests.
	 * 
	 * @param req  from client
	 * @param resp to client
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		Thread.currentThread().setName("Receiver");
		LOG.info("********************** Begin message "
				+ " processing **********************");
		// process the request
		if (!processRequest (req, resp))
			serverError (resp, "Failed to process POST request");
		LOG.info("******************* Completed message "
				+ "processing ********************");
	}

	/**
	 * Process one ebXML request
	 * 
	 * @param req from client
	 * @param resp to client
	 * @return true if response got sent
	 */
	private boolean processRequest (HttpServletRequest req, HttpServletResponse resp)
	{
		if (status.equals ("stopped"))
			return serverError (resp, "Receiver is stopped");
		MimeContent mimeRequest = null;
		// get the request and Mime parse it
		try
		{
			mimeRequest = MimeReceiver.receive (req);
		}
		catch (Exception e)
		{
			return serverError (resp, "Failed parsing ebXML message " + e.getLocalizedMessage());
		}
		LOG.debug("Request: \n" + mimeRequest.toString());
		// get message parts
		MimeContent[] parts = mimeRequest.getMultiParts();
		// check the parts
		if (parts == null)
			return serverError (resp, "Not a multi-part Mime request");
		// get the ebXML soap part
		// LOG.debug("Loading request: " + parts[0].getBody());
		SoapXml soap = new SoapXml (parts[0].getBody());
		// make sure we got ebXML
		if (!soap.ok ())
		{
			return serverError (resp, "Request is not ebXML SOAP");
		}
		soap.beautify(2);
		LOG.debug ("Request " + parts.length + " parts:" + soap.toString());
		// check for a valid CPA
		File cpa = new File (cpadir + soap.getCPAId () + ".xml");
		if (!cpa.exists ())
		{
			// TODO no such CPA
			return serverError (resp, "CPA " + soap.getCPAId() + " not found");
		}
  	try
		{
  		// check the reply cache
  		MimeContent mimeResponse = replies.get (soap);
  		// if not found then process the request
  		if (mimeResponse == null)
  		{
  			// get and run a proccessor
  			String serviceAction = soap.getService() + ":" + soap.getAction();
  			ReceiverProcessor processor = processors.get(serviceAction);
  			if (processor == null)
  				return serverError (resp, "Unknown service " + serviceAction);
			  mimeResponse = processor.process(soap, parts);
  		}
			if (mimeResponse == null)
				return serverError (resp, "Could not respond");
			// fill in headers and send off the response
			String[] h = mimeResponse.getHeaders();
			for (int i = 0; i < h.length; i++)
			{
				String[] hdr = h[i].split(": ");
				resp.setHeader(hdr[0], hdr[1]);
			}
			resp.getWriter().print(mimeResponse.getBody());
			// add it to the cache
			replies.put(soap, mimeResponse);
		}
		catch (IOException e)
		{
			return serverError (resp, "Failed writing response " + e.getLocalizedMessage());
		}
		return true;
	}
	
	/**
	 * General server 500 errors are reported back to clients here
	 * @param resp for this request
	 * @param msg to include in the response
	 * @return true if successful
	 */
	private boolean serverError (HttpServletResponse resp, String msg)
	{
		LOG.error(msg);
		try
		{
		  resp.sendError (HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
		}
		catch (IOException e)
		{
			LOG.error("Could not send response", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Clean up and die...
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	public void destroy()
	{
		shutdown ();
		LOG.info("Receiver Exiting...");
		super.destroy();
	}

	/**
	 * set up our thread name, load the configuration, intialize, and go...
	 * @throws exception if this doesn't work
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init() throws ServletException
	{
	  configName = getServletContext().getInitParameter("Configuration");
		if (!startup ())
		{
			throw (new ServletException ("Fatal error: error initializing jPhineas Receiver"));
		}
		status = "ready";
		LOG.info ("jPhineas Receiver Ready");
	}
}
