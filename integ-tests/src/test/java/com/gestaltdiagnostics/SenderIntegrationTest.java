package com.gestaltdiagnostics;

import java.io.File;
import java.security.KeyStore;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import tdunnick.jphineas.util.CertKeyPair;

public class SenderIntegrationTest {
	@ClassRule
	public static TemporaryFolder tmpFolder = new TemporaryFolder();
	private static JPhineasServer server;

	@BeforeClass
	public static void setup() throws Exception {
		KeyStore ks = CertKeyPair.generate().toJKS("alias");
		server = new JPhineasServer(8080, false, ks);
		server.start();
	}

	@AfterClass
	public static void teardown() {
		server.stop();
	}
	
	@Test
	public void testSending() {
		System.out.println(server.getReceiverUrl());
	}
}
