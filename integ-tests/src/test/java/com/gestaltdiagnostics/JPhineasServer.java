package com.gestaltdiagnostics;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import tdunnick.jphineas.config.ReceiverConfig;
import tdunnick.jphineas.receiver.Receiver;

/**
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class JPhineasServer {
	private Server server;
	private int port;
	private boolean needClientAuth;
	private KeyStore ks;

	private static final String RECEIVER_PATH = "/receiver";

	public JPhineasServer(int port, boolean needClientAuth, KeyStore ks) {
		this.port = port;
		this.needClientAuth = needClientAuth;
		this.ks = ks;
	}

	public void start() {
		server = new Server();
		HandlerList handlers = new HandlerList();
		server.setHandler(handlers);
		
		SslContextFactory.Server sslFactory = new SslContextFactory.Server();
		sslFactory.setNeedClientAuth(needClientAuth);
		sslFactory.setKeyStore(ks);
		sslFactory.setKeyStorePassword("");
		sslFactory.setTrustStore(ks);
		sslFactory.setTrustStorePassword("");

		ServerConnector connector = new ServerConnector(server, sslFactory);
		connector.setHost("localhost");
		connector.setPort(port);
		server.addConnector(connector);
		
		ServletHandler receiverServletHandler = new ServletHandler();
		ServletHolder holder = new ServletHolder(Receiver.class);
		String receiverConfFile = copyConfig("receiver.xml");
		holder.setInitParameter("Configuration", receiverConfFile);
		receiverServletHandler.addServletWithMapping(holder, RECEIVER_PATH);
		handlers.addHandler(receiverServletHandler);
		
		try {
			server.start();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		if(server != null) {
			try {
				server.stop();
			}
			catch(Exception e) {
				// ignore
			}
		}
	}

	public String getReceiverUrl() {
		return String.format("https://localhost:%d%s", port, RECEIVER_PATH);
	}

	private String copyConfig(String resourceName) {
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(resourceName);

			ReceiverConfig rc = new ReceiverConfig();
			boolean loaded = rc.load(stream);
			if(!loaded) {
				throw new IllegalStateException("failed to load the receiver configuration");
			}

			stream.close();

			File f = File.createTempFile("receiver", "xml");
			f.deleteOnExit();
			rc.save(f);
			
			return f.getAbsolutePath();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
