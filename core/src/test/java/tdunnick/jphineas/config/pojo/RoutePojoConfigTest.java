package tdunnick.jphineas.config.pojo;

import org.junit.Test;
import static org.junit.Assert.*;

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
}
