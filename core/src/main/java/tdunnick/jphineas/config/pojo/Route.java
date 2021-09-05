package tdunnick.jphineas.config.pojo;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.security.AnyTypePermission;

import tdunnick.jphineas.config.RouteConfig;

/**
 * In-memory configuration of a "route"
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
@XStreamAlias("Route")
public class Route {
	@XStreamAlias("Name")
	private String name;

	@XStreamAlias("PartyId")
	private String partyId;
	
	// Collaboration Protocol Agreement
	@XStreamAlias("Cpa")
	private String cpa;
	
	@XStreamAlias("Host")
	private String host;
	
	@XStreamAlias("Path")
	private String path;
	
	@XStreamAlias("Port")
	private int port;
	
	@XStreamAlias("Protocol")
	private String protocol;

	@XStreamAlias("TrustStore")
	private String trustStore;

	@XStreamAlias("TrustStorePassword")
	private String trustStorePassword;
	
	@XStreamAlias("Timeout")
	private int timeout;
	
	@XStreamAlias("Retry")
	private int retry;
	
	@XStreamAlias("Authentication")
	private Authentication authentication;
	
	@XStreamAlias("ChunkSize")
	private int chunkSize;

	private static final XStream serializer;
	
	static {
		serializer = new XStream();
		serializer.addPermission(AnyTypePermission.ANY);
		serializer.processAnnotations(new Class[] {Route.class, Authentication.class, BasicAuthentication.class, ClientCertAuthentication.class});
	}

	public Route() {
	}

	public RouteConfig toXmlConfig() {
		String xml = serializer.toXML(this);
		RouteConfig rc = new RouteConfig();
		boolean loaded = rc.load(xml);
		if(!loaded) {
			new IllegalArgumentException("failed to convert from POJO configuration to XML based RouteConfig");
		}
		return rc;
	}

	
	@Override
	public String toString() {
		return serializer.toXML(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getCpa() {
		return cpa;
	}

	public void setCpa(String cpa) {
		this.cpa = cpa;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public Authentication getAuthentication() {
		return authentication;
	}

	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
}
