/**
 * Raul Barbosa 2014-11-07
 */
package hey.model;

import rmiserver.ServerMethods;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class HeyBean {
	private ServerMethods server;
	private String username; // username and password supplied by the user
	private String password;

	public HeyBean() {
		try {
			server = (ServerMethods) Naming.lookup("server");
		}
		catch(NotBoundException|MalformedURLException|RemoteException e) {
			e.printStackTrace(); // what happens *after* we reach this line?
		}
	}

	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
}
