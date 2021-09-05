package tdunnick.jphineas.config.pojo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class BasicAuthentication implements Authentication {
	@XStreamAlias("Id")
	private String authId;

	@XStreamAlias("Password")
	private String password;

	@XStreamAlias("Type")
	private String type = "basic";

	public BasicAuthentication(String authId, String password) {
		this.authId = authId;
		this.password = password;
	}

	protected BasicAuthentication() {
	}

	public String getAuthId() {
		return authId;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getType() {
		return type;
	}
}
