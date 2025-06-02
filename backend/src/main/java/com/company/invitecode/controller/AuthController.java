package com.company.invitecode.controller;

import com.company.invitecode.config.JwtTokenProvider;
import com.company.invitecode.dto.request.LoginRequest;
import com.company.invitecode.dto.response.ApiResponse;
import com.company.invitecode.dto.response.TokenResponse;
import com.company.invitecode.model.User;
import com.company.invitecode.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("收到登录请求: {}", loginRequest.getUsername());
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            
            if (userOpt.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
                log.warn("登录失败: 用户名或密码错误");
                return ResponseEntity.ok(ApiResponse.error("用户名或密码错误"));
            }
            
            User user = userOpt.get();
            
            List<String> roles = new ArrayList<>();
            if (user.isAdmin()) {
                roles.add("ROLE_ADMIN");
            }
            roles.add("ROLE_USER");
            
            String token = jwtTokenProvider.createToken(user.getUsername(), roles);
            
            TokenResponse tokenResponse = new TokenResponse(token, user.getUsername(), roles);
            log.info("用户 {} 登录成功", user.getUsername());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", tokenResponse));
        } catch (Exception e) {
            log.error("登录过程中发生错误", e);
            return ResponseEntity.ok(ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }
} 