package tdunnick.jphineas.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * A utility class to generate X509Certificates and KeyStores.
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class CertKeyPair {
	private byte[] certificate;
	private byte[] privateKey;

	public CertKeyPair(byte[] certificate, byte[] privateKey) {
		this.certificate = certificate;
		this.privateKey = privateKey;
	}

	public static CertKeyPair generate() {
		try {
			X500Name subject = new X500Name("CN=localhost,OU=dev,O=GestaltDiagnostics,O=US");
			Date notBefore = new Date();
			long yearInMillis = (60 * 60 * 24 * 365 * 1000L);
			Date notAfter = new Date(notBefore.getTime() + (10 * yearInMillis));
			BigInteger serial = new BigInteger(64, new SecureRandom());
			
			RSAKeyGenerationParameters kgp = new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), 2048, PrimeCertaintyCalculator.getDefaultCertainty(2048));
			RSAKeyPairGenerator rpg = new RSAKeyPairGenerator();
			rpg.init(kgp);
			
			AsymmetricCipherKeyPair ackp = rpg.generateKeyPair();
			
			SubjectPublicKeyInfo spki = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(ackp.getPublic());
			X509v3CertificateBuilder builder = new X509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject, spki);
			
			AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
			AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
			
			ContentSigner signBuilder = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(ackp.getPrivate());
			X509CertificateHolder certHolder = builder.build(signBuilder);
			byte[] encodedPrivateKey = PrivateKeyInfoFactory.createPrivateKeyInfo(ackp.getPrivate()).getEncoded();
			
			return new CertKeyPair(certHolder.getEncoded(), encodedPrivateKey);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public KeyStore toJKS(String alias, String password) {
		try {
			Collection certs = new CertificateFactory().engineGenerateCertificates(new ByteArrayInputStream(certificate));
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey pk = kf.generatePrivate(keySpec);

			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(null, password.toCharArray());
			
			ks.setKeyEntry(alias, pk, password.toCharArray(), new Certificate[] { (Certificate)certs.iterator().next()});
			
			return ks;
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	public byte[] getCertificate() {
		return certificate;
	}

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public String getCertificateEncoded() {
		return toPem("CERTIFICATE", certificate);
	}

	public String getPrivateKeyEncoded() {
		return toPem("PRIVATE KEY", privateKey);
	}

	private String toPem(String header, byte[] data) {
		try {
			StringWriter sw = new StringWriter();
			PemWriter pw = new PemWriter(sw);
			pw.writeObject(new PemObject(header, data));
			pw.close();
			return sw.toString();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
