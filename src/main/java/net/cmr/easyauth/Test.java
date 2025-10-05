package net.cmr.easyauth;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class Test {
    public static void main(String[] args) {
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String jwt = Jwts.builder().signWith(secretKey).setId("123").setSubject("helloworld").compact();
        System.out.println(jwt);
        System.out.println(Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwt));
    }
    
}
