package com.credera.bootjpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String email;
	private String password;
	private boolean admin;
	
	public User() {
		this.email = "default@default.com";
		this.password = "default";
		this.admin = false;
	}
	
	public User(String email, String password, boolean admin) {
		this.email = email;
		this.password = password;
		this.admin = admin;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public long getId() {
		return id;
	}
	
	public String toString() {
		return email + ", " + password;
	}

}
