package com.gestaltdiagnostics;

import java.io.File;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.mirth.connect.client.core.Client;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.model.DashboardStatus;

/**
 * 
 * @author Kiran Ayyagari (kayyagari@apache.org)
 */
public class MirthConnectServer {
    private Process process;

    private String host = "127.0.0.1";
    private int port = 8443;
    

    private String url = "https://" + host + ":" + port;
    private Client client;
    
    private Charset utf8 = StandardCharsets.UTF_8;
    
    public void start() {
    	try {
    		String pwd = System.getProperty("user.dir");
    		File dir =  new File(pwd + File.separator + "target" + File.separator + "Mirth Connect");
    		
    		Runtime rt = Runtime.getRuntime();
    		
    		process = rt.exec(new String[]{"./mcserver"}, null, dir);
    		
    		// clean up via a shutdownhook
    		rt.addShutdownHook(new Thread(
    				new Runnable() {
    					@Override
    					public void run() {
    						process.destroyForcibly();
    					}
    				}
    				));
    		
    		// wait until the server starts
    		while(true) {
    			try {
    				Socket sock = new Socket(host, port);
    				System.out.println("Mirth Connect started");
    				sock.close();
    				break;
    			}
    			catch(Exception e) {
    				Thread.sleep(500);
    			}
    		}
    	}
    	catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public Client getClient() {
    	try {
    		if(client == null) {
    			client = new Client(url);
    			client.login("admin", "admin");
    		}
    		
    		return client;
    	}
    	catch(Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    public void waitUntilChannelDeployed(String channelId) {
        int count = 25; // maximum number of tries
        
        DeployedState state = DeployedState.UNKNOWN;

        while(count > 0) {
            count--;
            
            try {
                 DashboardStatus status = client.getChannelStatus(channelId);
                 state = status.getState();
            }
            catch(Exception e) {
                // keep trying
                System.out.println(e.getMessage());
            }
            
            if(state == DeployedState.STARTED) {
            	System.out.println(String.format("channel %s deployed", channelId));
                break;
            }
            else {
            	try {
            		Thread.sleep(1000);
            	}
            	catch(Exception e) {
            		throw new RuntimeException(e);
            	}
            }
        }
        
        if(state != DeployedState.STARTED) {
        	throw new IllegalStateException("channel wasn't deployed");
        }
    }
}
