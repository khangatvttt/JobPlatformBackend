package com.jobplatform.configs;

import com.jobplatform.models.UserAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;


    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 AuthenticationProvider authenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.DELETE,"/jobs/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.POST,"/jobs/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name())
                    .requestMatchers(HttpMethod.POST,"/applications/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name())
                    .requestMatchers(HttpMethod.GET,"/applications").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.DELETE,"/applications").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.POST,"/companies/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.DELETE,"/companies/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.PATCH,"/companies/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.POST,"/cvs/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name())
                    .requestMatchers(HttpMethod.PATCH,"/cvs/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name())
                    .requestMatchers(HttpMethod.DELETE,"/cvs/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.POST,"/interview-invitations/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.PATCH,"/interview-invitations/**").hasAnyAuthority(UserAccount.Role.ROLE_RECRUITER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers("/job-saves/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name())
                    .requestMatchers(HttpMethod.POST,"/reviews/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.DELETE,"/reviews/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.PATCH,"/reviews/**").hasAnyAuthority(UserAccount.Role.ROLE_JOB_SEEKER.name(), UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers("/statistics/**").hasAnyAuthority(UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers(HttpMethod.GET,"/users").hasAnyAuthority(UserAccount.Role.ROLE_ADMIN.name())
                    .requestMatchers("/auth/**","/grantcode/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/jobs/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/companies/**").permitAll()
                    .requestMatchers("/momo-payment/**").permitAll()
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("Authorization","Content-Type"));

        configuration.setExposedHeaders(List.of("X-Total-Pages","X-Total-Elements"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**",configuration);

        return source;
    }
}