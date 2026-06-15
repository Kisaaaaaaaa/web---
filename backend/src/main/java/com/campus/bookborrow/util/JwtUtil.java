package com.campus.bookborrow.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具类
 * 负责生成、解析、验证 JWT Token
 *
 * @author Campus Book Borrow Team
 * @since 2026-06-08
 */
@Component
public class JwtUtil {

    private final String secretKey;
    private final long expiration;
    private final byte[] signingKey;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration}") long expiration) {
        this.secretKey = secret;
        this.expiration = expiration;
        // 对原始密钥做 Base64 编码，作为 HS256 签名密钥
        this.signingKey = DatatypeConverter
                .parseBase64Binary(DatatypeConverter.printBase64Binary(secret.getBytes()));
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String username, String roleCode, String realName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);
        claims.put("realName", realName);

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration))
                .signWith(SignatureAlgorithm.HS256, signingKey)
                .compact();
    }

    /**
     * 解析 JWT Token，获取 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public String getRoleCode(String token) {
        return parseToken(token).get("roleCode", String.class);
    }
}
