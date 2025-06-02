package com.company.invitecode.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String token = jwtTokenProvider.resolveToken(request);
            log.debug("从请求中提取的令牌: {}", token);
            
            if (token != null && jwtTokenProvider.validateToken(token)) {
                log.debug("令牌有效，正在设置认证信息");
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("认证信息已设置: {}", auth);
            } else {
                log.debug("无效的令牌或令牌不存在");
            }
        } catch (Exception ex) {
            log.error("无法设置用户认证", ex);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
} 