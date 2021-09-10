package tdunnick.jphineas.config.pojo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static tdunnick.jphineas.config.pojo.ClientCertAuthentication.ALIAS;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import org.junit.Test;

import tdunnick.jphineas.config.RouteConfig;
import tdunnick.jphineas.util.CertKeyPair;

/**
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class RoutePojoConfigTest {
	@Test
	public void testPojoToXmlConfig() {
		Route route = new Route();
		BasicAuthentication ba = new BasicAuthentication("username", "password");
		route.setAuthentication(ba);
		route.setChunkSize(2);
		route.setCpa("cpaId");
		route.setHost("localhost");
		route.setName("route70");
		route.setPartyId("partyId1");
		route.setPath("/tmp");
		route.setPort(8080);
		route.setProtocol("https");
		route.setRetry(2);
		route.setTimeout(100);
		
		RouteConfig rc = route.toXmlConfig();
		assertNotNull(rc);
		
		assertEquals("basic", ba.getType());
		assertEquals(ba.getType(), rc.getAuthenticationType());
		assertEquals(ba.getAuthId(), rc.getAuthenticationId());
		assertEquals(ba.getPassword(), rc.getAuthenticationPassword());

		assertEquals(route.getChunkSize(), rc.getChunkSize());
		assertEquals(route.getCpa(), rc.getCpa());
		assertEquals(route.getHost(), rc.getHost());
		assertEquals(route.getName(), rc.getName());
		assertEquals(route.getPartyId(), rc.getPartyId());
		assertEquals(route.getPath(), rc.getPath());
		assertEquals(route.getPort(), rc.getPort());
		assertEquals(route.getProtocol(), rc.getProtocol());
		assertEquals(route.getRetry(), rc.getRetry());
		assertEquals(route.getTimeout(), rc.getTimeout());
	}
	
	@Test
	public void testClientCertConfigTest() throws Exception {
		CertKeyPair ckp = CertKeyPair.generate();
		String privKey = ckp.getPrivateKeyEncoded();
		String cert = ckp.getCertificateEncoded();
		
		ClientCertAuthentication cca = ClientCertAuthentication.fromCertAndKeyInPemFormat(cert, privKey);
		assertNotNull(cca.getKeystorePath());
		assertNotNull(cca.getPassword());
		assertEquals("clientcert", cca.getType());
		
		File f = new File(cca.getKeystorePath());
		assertTrue(f.exists());
		
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream stream = new FileInputStream(f);
		ks.load(stream, cca.getPassword().toCharArray());
		stream.close();
		
		assertArrayEquals(ckp.getCertificate(), ks.getCertificate(ALIAS).getEncoded());
		assertArrayEquals(ckp.getPrivateKey(), ks.getKey(ALIAS, cca.getPassword().toCharArray()).getEncoded());
	}
}
