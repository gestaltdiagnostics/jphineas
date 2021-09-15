package com.gestaltdiagnostics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.util.ConfigurationProperty;

import tdunnick.jphineas.xml.ResponseXml;

/**
 * Integration test using MirthConnect.
 *
 * Note: Before executing this test make sure the MirthConect installer tarball is available in
 * the local Maven repo. If it is not available please execute <i>upload-mc-to-maven.sh</i> script first.
 *
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class SenderMirthConnectIntegrationTest extends IntegrationTestBase {
	private MirthConnectServer mcServer;
	private Client mcClient;
	private String channelId;

	@Before
	public void start() throws Exception {
		mcServer = new MirthConnectServer();
		mcServer.start();
		mcClient = mcServer.getClient();

		Map<String, ConfigurationProperty> configMap = mcClient.getConfigurationMap();
		configMap.put("clientCertPem", new ConfigurationProperty(clientCertPem, "X509 certificate of client"));
		configMap.put("clientKeyPem", new ConfigurationProperty(clientKeyPem, "privatekey of client"));
		configMap.put("trustStorePath", new ConfigurationProperty(serverKeystoreTempFile.getAbsolutePath(), "truststore containing jPhineas server's certificate"));
		configMap.put("trustStorePassword", new ConfigurationProperty(KEYSTORE_PASSWORD, "truststore password"));
		
		mcClient.setConfigurationMap(configMap);
		
		// sanity check
		configMap = mcClient.getConfigurationMap();
		ConfigurationProperty clientCertPemConfProp = configMap.get("clientCertPem");
		assertNotNull(clientCertPemConfProp);
		assertEquals(clientCertPem, clientCertPemConfProp.getValue());
		
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        serializer.init("3.11.0");

		// deploy channel
        // it is easier to load an exported channel than creating one programmatically in this specific case
		String channelXml = getResourceContent("jphineas-test-channel.xml");
		Channel channel = serializer.deserialize(channelXml, Channel.class);
		boolean created = mcClient.createChannel(channel);
		assertTrue(created);
		
		channelId = channel.getId();
		mcClient.deployChannel(channelId);
		mcServer.waitUntilChannelDeployed(channelId);
	}
	
	@Test
	public void testUsingChannel() throws Exception {
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://localhost:8480/proxy/");
		post.setEntity(new StringEntity("this is a message to be delivered to a jPhineas receiver"));
		HttpResponse resp = httpClient.execute(post);
		assertEquals(200, resp.getStatusLine().getStatusCode());
		String ebxmlResp = EntityUtils.toString(resp.getEntity());
		ResponseXml respXml = new ResponseXml(ebxmlResp);
		assertTrue(respXml.ok());
	}
}
