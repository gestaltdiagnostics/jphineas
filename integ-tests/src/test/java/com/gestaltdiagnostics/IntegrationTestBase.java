package com.gestaltdiagnostics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import tdunnick.jphineas.config.pojo.ClientCertAuthentication;
import tdunnick.jphineas.config.pojo.Route;

/**
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public abstract class IntegrationTestBase {
	@ClassRule
	public static TemporaryFolder tmpFolder = new TemporaryFolder();

	protected static JPhineasServer server;
	protected static KeyStore clientKeystore;
	protected static KeyStore serverKeystore;
	protected static File serverKeystoreTempFile;
	protected static String clientCertPem;
	protected static String clientKeyPem;

	public static String KEYSTORE_PASSWORD = "changeit";
	public static int JPHINEAS_SERVER_PORT = 9493;

	protected static Route route;

	@BeforeClass
	public static void setup() throws Exception {
		//System.setProperty("javax.net.debug", "all");

		File tmpDir = tmpFolder.newFolder();
		clientKeystore = openKeystore("client.jks", KEYSTORE_PASSWORD);
		serverKeystore = openKeystore("server.jks", KEYSTORE_PASSWORD);

		serverKeystoreTempFile = new File(tmpDir, "server.jks");
		OutputStream out = new FileOutputStream(serverKeystoreTempFile);
		serverKeystore.store(out, KEYSTORE_PASSWORD.toCharArray());
		out.close();
		System.out.println(serverKeystoreTempFile.getAbsolutePath());

		server = new JPhineasServer(JPHINEAS_SERVER_PORT, true, serverKeystore, clientKeystore, KEYSTORE_PASSWORD, tmpDir);
		server.start();

	    clientCertPem = getResourceContent("client-cert.pem");
	    clientKeyPem = getResourceContent("client-key.pem");
		
	    route = new Route();
	    ClientCertAuthentication ca = ClientCertAuthentication.fromCertAndKeyInPemFormat(clientCertPem, clientKeyPem);
		route.setAuthentication(ca);
		route.setChunkSize(0);
		route.setCpa("cpaId");
		route.setHost("localhost");
		route.setName("route70");
		route.setPartyId("partyId1");
		route.setPath(JPhineasServer.RECEIVER_PATH);
		route.setPort(JPHINEAS_SERVER_PORT);
		route.setProtocol("https");
		route.setRetry(0);
		route.setTimeout(0);
		route.setTrustStore(serverKeystoreTempFile.getAbsolutePath());
		route.setTrustStorePassword(KEYSTORE_PASSWORD);
	}

	@AfterClass
	public static void teardown() {
		server.stop();
	}
	
	public static KeyStore openKeystore(String name, String password) {
		try(InputStream stream = IntegrationTestBase.class.getClassLoader().getResourceAsStream(name)) {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(stream, password.toCharArray());
			return ks;
		}
		catch(Exception e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	static String getResourceContent(String name) {
		try(InputStream stream = IntegrationTestBase.class.getClassLoader().getResourceAsStream(name)) {
			ByteArrayOutputStream sw = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while(true) {
				int len = stream.read(buf);
				if(len <= 0) {
					break;
				}
				sw.write(buf, 0, len);
			}
			return new String(sw.toByteArray(), StandardCharsets.UTF_8);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
