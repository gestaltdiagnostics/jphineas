package com.gestaltdiagnostics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import tdunnick.jphineas.ebxml.EbXmlRequest;
import tdunnick.jphineas.sender.ebxml.EbXmlRouteConnection;
import tdunnick.jphineas.xml.ResponseXml;

/**
 * Integration test for sender.
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class SenderStandaloneIntegrationTest extends IntegrationTestBase {
	private String service = "defaultservice";
	private String action = "defaultaction";
	private String recordId;
	private String arguments = "";
	private String messageRecipient = "test-server";
	private String certificateUrl = "";
	
	@Before
	public void newRecordId() {
		recordId = UUID.randomUUID().toString();
	}

	@Test
	public void testSending() {
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
		String message = "this is a message"; // a message with 17 bytes
		
		route.setChunkSize(8); // should result in 3 chunks
		EbXmlRouteConnection conn = new EbXmlRouteConnection(route);
		conn.open();
		EbXmlRequest req = new EbXmlRequest(service, action, recordId, arguments, messageRecipient, certificateUrl, message);
		ResponseXml resp = conn.send(req);
		assertNotNull(resp);
		assertTrue(resp.ok());
		conn.close();
	}

	@Test
	@Ignore("test server is not keeping the connection open, needs further investigation")
	public void testSendingMultipleMessagesUsingSameConnection() {
		String message = "hello!";

		route.setChunkSize(0);
		EbXmlRouteConnection conn = new EbXmlRouteConnection(route);
		conn.open();

		int total = 1000;
		for(int i=0; i < total; i++) {
			assertTrue(conn.isValid());
			EbXmlRequest req = new EbXmlRequest(service, action, recordId, arguments, messageRecipient, certificateUrl, message);
			ResponseXml resp = conn.send(req);
			assertNotNull(resp);
			assertTrue(resp.ok());
		}
		conn.close();
	}
}
