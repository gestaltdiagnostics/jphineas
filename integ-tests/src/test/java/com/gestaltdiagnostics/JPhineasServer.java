package com.gestaltdiagnostics;

import static com.gestaltdiagnostics.IntegrationTestBase.JPHINEAS_SERVER_PORT;
import static com.gestaltdiagnostics.IntegrationTestBase.KEYSTORE_PASSWORD;
import static com.gestaltdiagnostics.IntegrationTestBase.openKeystore;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;

import org.apache.log4j.Logger;
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
	private KeyStore ts;
	private File tmpDir;
	private String keystorePassword;

	public static final String RECEIVER_PATH = "/receiver";

	private static final Logger LOG = Logger.getLogger(JPhineasServer.class);
	
	public JPhineasServer(int port, boolean needClientAuth, KeyStore ks, KeyStore ts, String keystorePassword, File tmpDir) {
		this.port = port;
		this.needClientAuth = needClientAuth;
		this.ks = ks;
		this.ts = ts;
		this.tmpDir = tmpDir;
		this.keystorePassword = keystorePassword;
	}

	public void start() {
		server = new Server();
		HandlerList handlers = new HandlerList();
		server.setHandler(handlers);
		
		SslContextFactory.Server sslFactory = new SslContextFactory.Server();
		sslFactory.setNeedClientAuth(needClientAuth);
		sslFactory.setKeyStore(ks);
		sslFactory.setKeyStorePassword(keystorePassword);
		sslFactory.setTrustStore(ts);
		sslFactory.setTrustStorePassword(keystorePassword);

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
			System.out.println("jPhineas receiver started");
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

			// first load the stock config (this doesn't contain a default directory)
			ReceiverConfig rc = new ReceiverConfig();
			rc.load(stream);
			stream.close();

			// then set the temporary folder as the default directory
			rc.setDefaultDir(tmpDir);

			// and save
			File f = new File(tmpDir, "receiver.xml");
			rc.save(f);

			// finally reload. Now, the new temp directory is in use.
			rc = new ReceiverConfig();
			boolean loaded = rc.load(f);
			if(!loaded) {
				throw new IllegalStateException("failed to load the receiver configuration");
			}

			System.out.println(rc.getPayloadDirectory());
			return f.getAbsolutePath();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) {
		KeyStore clientKeystore = openKeystore("client.jks", KEYSTORE_PASSWORD);
		KeyStore serverKeystore = openKeystore("server.jks", KEYSTORE_PASSWORD);
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		JPhineasServer server = new JPhineasServer(JPHINEAS_SERVER_PORT, true, serverKeystore, clientKeystore, KEYSTORE_PASSWORD, tmpDir);
		server.start();
	}
}
