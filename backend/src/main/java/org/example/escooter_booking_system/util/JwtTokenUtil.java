package org.example.escooter_booking_system.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.escooter_booking_system.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    // 从 application.properties 读取过期时间（毫秒）
    @Value("${jwt.expiration}")
    private Long expiration;

    // 直接生成并持有 SecretKey 实例
    private SecretKey signingKey;

    // 使用 PostConstruct 或直接初始化来生成密钥
    @PostConstruct // Or initialize directly: private SecretKey signingKey =
                   // Keys.secretKeyFor(SignatureAlgorithm.HS512);
    public void init() {
        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        System.out.println("Generated Signing Key Algorithm: " + this.signingKey.getAlgorithm());
        System.out.println("Generated Signing Key Format: " + this.signingKey.getFormat());
        System.out.println("Generated Signing Key Length (bytes): " + this.signingKey.getEncoded().length);
        System.out.println("Generated Signing Key Length (bits): " + this.signingKey.getEncoded().length * 8);
    }

    // getSigningKey 方法现在直接返回持有的密钥实例
    private SecretKey getSigningKey() {
        return this.signingKey; // 返回内部生成的密钥
    }

    // 从 token 中获取用户名 (Subject)
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // 从 token 中获取过期时间
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // 从 token 中获取指定的 claim
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 获取 token 中的所有 claims
    private Claims getAllClaimsFromToken(String token) {
        // 使用 parserBuilder() 来适应 SecretKey 对象
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    // 检查 token 是否过期
    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    // 为用户生成 token
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        return doGenerateToken(claims, user.getUsername());
    }

    // 创建 token
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                // 直接传递 SecretKey 对象给 signWith
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 验证 token 是否有效
    public Boolean validateToken(String token, User user) {
        final String username = getUsernameFromToken(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }
}