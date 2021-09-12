package com.gestaltdiagnostics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.TemporaryFolder;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.config.pojo.ClientCertAuthentication;
import tdunnick.jphineas.config.pojo.Route;
import tdunnick.jphineas.ebxml.EbXmlRequest;
import tdunnick.jphineas.sender.ebxml.EbXmlRouteConnection;
import tdunnick.jphineas.util.CertKeyPair;
import tdunnick.jphineas.xml.ResponseXml;

/**
 * Integration test for sender.
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class SenderIntegrationTest {
	@ClassRule
	public static TemporaryFolder tmpFolder = new TemporaryFolder();

	private static JPhineasServer server;
	private static KeyStore clientKeystore;
	private static KeyStore serverKeystore;
	private static File serverKeystoreFile;

	private static String keystorePassword = "changeit";
	private static Route route;

	@BeforeClass
	public static void setup() throws Exception {
		//System.setProperty("javax.net.debug", "all");

		File tmpDir = tmpFolder.newFolder();
		CertKeyPair clientKeypair = CertKeyPair.generate();
		clientKeystore = clientKeypair.toJKS("alias", keystorePassword);
		serverKeystore = CertKeyPair.generate().toJKS("alias", keystorePassword);

		serverKeystoreFile = new File(tmpDir, "server.jks");
		OutputStream out = new FileOutputStream(serverKeystoreFile);
		serverKeystore.store(out, keystorePassword.toCharArray());
		out.close();

		int port = 8080;
		server = new JPhineasServer(port, true, serverKeystore, clientKeystore, keystorePassword, tmpDir);
		server.start();

	    route = new Route();
		ClientCertAuthentication ca = ClientCertAuthentication.fromCertAndKeyInPemFormat(clientKeypair.getCertificateEncoded(), clientKeypair.getPrivateKeyEncoded());
		route.setAuthentication(ca);
		route.setChunkSize(0);
		route.setCpa("cpaId");
		route.setHost("localhost");
		route.setName("route70");
		route.setPartyId("partyId1");
		route.setPath(JPhineasServer.RECEIVER_PATH);
		route.setPort(port);
		route.setProtocol("https");
		route.setRetry(0);
		route.setTimeout(0);
		route.setTrustStore(serverKeystoreFile.getAbsolutePath());
		route.setTrustStorePassword(keystorePassword);
	}

	@AfterClass
	public static void teardown() {
		server.stop();
	}
	
	@Test
	public void testSending() {
		String service = "defaultservice";
		String action = "defaultaction";
		String recordId = UUID.randomUUID().toString();
		String arguments = "";
		String messageRecipient = "test-server";
		String certificateUrl = "";
		String message = "hello";

		route.setChunkSize(0);
		EbXmlRouteConnection conn = new EbXmlRouteConnection(route);
		conn.open();
		EbXmlRequest req = new EbXmlRequest(service, action, recordId, arguments, messageRecipient, certificateUrl, message);
		ResponseXml resp = conn.send(req);
		assertNotNull(resp);
		assertTrue(resp.ok());
		conn.close();
	}

	@Test
	public void testSendingWithChunking() {
		String service = "defaultservice";
		String action = "defaultaction";
		String recordId = UUID.randomUUID().toString();
		String arguments = "";
		String messageRecipient = "test-server";
		String certificateUrl = "";
		String message = "this is a message"; // a message with 17 bytes
		
		route.setChunkSize(8); // should result in 3 chunks
		EbXmlRouteConnection conn = new EbXmlRouteConnection(route);
		conn.open();
		EbXmlRequest req = new EbXmlRequest(service, action, recordId, arguments, messageRecipient, certificateUrl, message);
		ResponseXml resp = conn.send(req);
		System.out.println("----->\n" + resp);
		assertNotNull(resp);
		assertTrue(resp.ok());
		conn.close();
	}
}
