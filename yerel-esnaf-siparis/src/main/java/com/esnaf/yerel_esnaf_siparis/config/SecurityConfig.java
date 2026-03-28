package com.esnaf.yerel_esnaf_siparis.config;

import com.esnaf.yerel_esnaf_siparis.model.User;
import com.esnaf.yerel_esnaf_siparis.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            if (user == null) throw new UsernameNotFoundException("User not found");
            String roleName = "ROLE_" + user.getRole().name();
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    authorities
            );
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/style.css", "/script.js", "/", "/register", "/test", "/login", "/create-admin").permitAll()
                .requestMatchers("/api/users/register").permitAll()
                .requestMatchers("/api/products", "/api/products/{id}", "/api/products/search", "/api/products/category/**").permitAll()
                .requestMatchers("/api/categories", "/api/categories/**").permitAll()
                .requestMatchers("/api/reviews/product/**").permitAll()
                
                //(Müşteri)
                .requestMatchers("/cart", "/orders").hasRole("CUSTOMER")
                .requestMatchers("/api/checkout").hasRole("CUSTOMER")
                .requestMatchers("/api/coupons/validate").hasRole("CUSTOMER")
                
                // (Esnaf)
                .requestMatchers("/my-products", "/shop-orders", "/dashboard").hasRole("SHOP_OWNER")
                .requestMatchers("/api/products/my-products", "/api/products/**").hasRole("SHOP_OWNER")
                .requestMatchers("/api/dashboard/**").hasRole("SHOP_OWNER")
                .requestMatchers("/api/orders/shop-orders", "/api/orders/*/status").hasRole("SHOP_OWNER")
                
                // (müşterinin kendi siparişleri)
                .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
                
                // Admin only routes
                .requestMatchers("/api/admin/**", "/api/coupons", "/admin/**").hasRole("ADMIN")
                
                // (Herkes)
                .requestMatchers("/profile", "/api/profile/**", "/api/reviews").authenticated()
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login"));
        return http.build();
    }
}