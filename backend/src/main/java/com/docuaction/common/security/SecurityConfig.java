package com.docuaction.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationFilter jwtAuthenticationFilter,
		RestAuthenticationEntryPoint authenticationEntryPoint
	) throws Exception {
		return http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/health",
					"/api/auth/signup",
					"/api/auth/login",
					"/h2-console/**",
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				).permitAll()
				.anyRequest().authenticated())
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
			.httpBasic(httpBasic -> httpBasic.disable())
			.formLogin(formLogin -> formLogin.disable())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
