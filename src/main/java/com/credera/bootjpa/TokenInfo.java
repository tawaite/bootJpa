package com.credera.bootjpa;

import org.joda.time.DateTime;

public class TokenInfo {
	
	private String email;
    private DateTime issued;
    private DateTime expires;
    
    public String getUserId() {
        return email;
    }
    public void setUserId(String email) {
        this.email = email;
    }
    public DateTime getIssued() {
        return issued;
    }
    public void setIssued(DateTime issued) {
        this.issued = issued;
    }
    public DateTime getExpires() {
        return expires;
    }
    public void setExpires(DateTime expires) {
        this.expires = expires;
    }
}
