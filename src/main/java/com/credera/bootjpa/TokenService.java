package com.credera.bootjpa;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Instant;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;
import net.oauth.jsontoken.crypto.HmacSHA256Signer;
import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;

//Code based on http://stackoverflow.com/questions/23808460/jwt-json-web-token-library-for-java

public class TokenService {
	
	private static final String AUDIENCE = "NotReallyImportant";
	private static final String ISSUER = "CrederaBootJpaTest";
	private static final String SIGNING_KEY = "HehC9apbAdXP9rHGRu5ajeXg2NMb&(#$@^*)#@$&()@#$*)(&";
	
	//Creates a JWT
	public static String createJsonWebToken(String email, Long durationDays) {
		//Get the current time
		Calendar calendar = Calendar.getInstance();
		HmacSHA256Signer signer;
		
		try {
			signer = new HmacSHA256Signer(ISSUER, null, SIGNING_KEY.getBytes());
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
		
		//Configure the token
		JsonToken token = new JsonToken(signer);
		token.setAudience(AUDIENCE);
		token.setIssuedAt(new Instant(calendar.getTimeInMillis()));
		token.setExpiration(new Instant(calendar.getTimeInMillis() + 1000L * 60L * 60L * 24L * durationDays));
	
		//Configure the request object
		JsonObject request = new JsonObject();
		request.addProperty("email", email);
		
		JsonObject payload = token.getPayloadAsJsonObject();
		payload.add("info", request);
		
		try {
			return token.serializeAndSign();
		} catch (SignatureException e) {
			throw new RuntimeException(e);
		}

	}
	
	public static TokenInfo verifyToken(String token) {
		
		try {
			final Verifier hmacVerifier = new HmacSHA256Verifier(SIGNING_KEY.getBytes());
			
			VerifierProvider hmacLocator = new VerifierProvider() {

                @Override
                public List<Verifier> findVerifier(String id, String key){
                    return Lists.newArrayList(hmacVerifier);
                }
            };
            
            VerifierProviders locators = new VerifierProviders();
            
            locators.setVerifierProvider(SignatureAlgorithm.HS256, hmacLocator);
            
            net.oauth.jsontoken.Checker checker = new net.oauth.jsontoken.Checker(){

                @Override
                public void check(JsonObject payload) throws SignatureException {
                    // don't throw - allow anything
                }

            };
            
            JsonTokenParser parser = new JsonTokenParser(locators, checker);
            JsonToken jt;
            
            //Deserialize and throw an exception if invalid signature
            try {
                jt = parser.verifyAndDeserialize(token);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
            
            JsonObject payload = jt.getPayloadAsJsonObject();
            TokenInfo t = new TokenInfo();
            String issuer = payload.getAsJsonPrimitive("iss").getAsString();
            String emailString =  payload.getAsJsonObject("info").getAsJsonPrimitive("email").getAsString();
            
            //Make sure we gave out the key and the email exists
            if (issuer.equals(ISSUER) && !emailString.isEmpty()) {
                t.setUserId(new String(emailString));
                t.setIssued(new DateTime(payload.getAsJsonPrimitive("iat").getAsLong())); //iat -> issuedAt
                t.setExpires(new DateTime(payload.getAsJsonPrimitive("exp").getAsLong()));
                return t;
            } else {
                return null;
            }
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}

}
