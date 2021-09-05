package tdunnick.jphineas.config.pojo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class ClientCertAuthentication implements Authentication {
	@XStreamAlias("Unc")
	private String keystorePath;

	@XStreamAlias("Password")
	private String password;

	@XStreamAlias("Type")
	private String type = "clientcert";

	private static final Logger LOG = Logger.getLogger(ClientCertAuthentication.class);

	private ClientCertAuthentication(String keystorePath, String password) {
		this.keystorePath = keystorePath;
		this.password = password;
	}

	public static ClientCertAuthentication fromKeystoreFile(String keystorePath, String password) {
		return new ClientCertAuthentication(keystorePath, password);
	}

	public static ClientCertAuthentication fromCertAndKeyInPemFormat(String cert, String key) {
		CertPath cp = null;
		try {
			cp = CertificateFactory.getInstance("X509").generateCertPath(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
		}
		catch(Exception e) {
			LOG.warn("failed to parse certificate(s) from PEM content");
			LOG.warn("", e);
			throw new RuntimeException(e);
		}
		
		PEMParser parser = new PEMParser(new StringReader(key));
		PemObject pemObj = null;
		try {
			pemObj = parser.readPemObject();
		}
		catch(Exception e) {
			LOG.warn("failed to parse privatekey from PEM content");
			LOG.warn("", e);
			throw new RuntimeException(e);
		}

		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pemObj.getContent());
		Set<String> algos = Security.getAlgorithms("KeyFactory");

		PrivateKey pk = null;
		for(String name : algos) {
			try {
				KeyFactory kf = KeyFactory.getInstance(name);
				pk = kf.generatePrivate(keySpec);
				LOG.debug("parse privatekey using algorithm: " + name);
				break;
			}
			catch(Exception e) {
				// ignore
			}
		}
		
		if(pk == null) {
			throw new IllegalArgumentException("could not parse the privatekey using any of the supported algorithms");
		}
		
		String password = UUID.randomUUID().toString();
		File f = null;
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(null, null);
			
			List<? extends Certificate> certs = cp.getCertificates();
			ks.setKeyEntry("alias", pk, null, (Certificate[])certs.toArray());
			
			MessageDigest md = MessageDigest.getInstance("md5");
			byte[] digest = md.digest(certs.get(0).getEncoded());
			String hash = Hex.toHexString(digest);
			f = File.createTempFile(hash, ".jks");
			f.deleteOnExit();
			
			FileOutputStream out = new FileOutputStream(f);
			ks.store(out, password.toCharArray());
			out.close();
		}
		catch(Exception e) {
			LOG.warn("failed to create a temporary keystore with the PEM data");
			LOG.warn("", e);
			throw new RuntimeException(e);
		}

		return new ClientCertAuthentication(f.getAbsolutePath(), password);
	}

	public String getType() {
		return type;
	};
}
