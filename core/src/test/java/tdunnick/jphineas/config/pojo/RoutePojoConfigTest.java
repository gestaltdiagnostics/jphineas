package tdunnick.jphineas.config.pojo;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.junit.Test;

import tdunnick.jphineas.config.RouteConfig;

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
		X500Name subject = new X500Name("CN=localhost,OU=dev,O=GestaltDiagnostics,O=US");
		X500Name issuer = new X500Name("CN=kayyagari,OU=dev,O=GestaltDiagnostics,O=US");
		Date notBefore = new Date();
		long yearInMillis = (60 * 60 * 24 * 365 * 1000L);
		Date notAfter = new Date(notBefore.getTime() + (10 * yearInMillis));
		BigInteger serial = new BigInteger(64, new SecureRandom());

		RSAKeyGenerationParameters kgp = new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), 2048, PrimeCertaintyCalculator.getDefaultCertainty(2048));
		RSAKeyPairGenerator rpg = new RSAKeyPairGenerator();
		rpg.init(kgp);
		
		AsymmetricCipherKeyPair ackp = rpg.generateKeyPair();

		SubjectPublicKeyInfo spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(ackp.getPublic());
		X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, spki);

		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
		
		ContentSigner signBuilder = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(ackp.getPrivate());
		X509CertificateHolder certHolder = builder.build(signBuilder);
		
		byte[] encodedPrivateKey = PrivateKeyInfoFactory.createPrivateKeyInfo(ackp.getPrivate()).getEncoded();
		String privKey = toPem("PRIVATE KEY", encodedPrivateKey);
		String cert = toPem("CERTIFICATE", certHolder.getEncoded());
		
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
		
		assertArrayEquals(certHolder.getEncoded(), ks.getCertificate(ClientCertAuthentication.ALIAS).getEncoded());
		assertArrayEquals(encodedPrivateKey, ks.getKey(ClientCertAuthentication.ALIAS, "".toCharArray()).getEncoded());
	}
	
	private String toPem(String header, byte[] data) throws IOException {
		StringWriter sw = new StringWriter();
		PemWriter pw = new PemWriter(sw);
		pw.writeObject(new PemObject(header, data));
		pw.close();
		return sw.toString();
	}
}
