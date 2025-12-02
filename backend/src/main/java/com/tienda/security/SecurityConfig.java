package com.tienda.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;
    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;
    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authenticationProvider(authenticationProvider())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/dashboard/summary").permitAll()
                        // Productos: GET for all roles; write ops restricted
                        .requestMatchers(HttpMethod.GET, "/productos/**").hasAnyRole("ADMIN","EMPLEADO","SUPERVISOR")
                        .requestMatchers("/productos/**").hasAnyRole("ADMIN","SUPERVISOR")
                        .requestMatchers(HttpMethod.GET, "/clientes/**", "/proveedores/**").hasAnyRole("ADMIN","EMPLEADO","SUPERVISOR")
                        .requestMatchers("/clientes/**", "/proveedores/**").hasAnyRole("ADMIN","SUPERVISOR")
                        // Ventas y pagos de venta: ADMIN/EMPLEADO/SUPERVISOR
                        .requestMatchers("/ventas/**", "/pagos-venta/**").hasAnyRole("ADMIN","EMPLEADO","SUPERVISOR")
                        // Reportes: ADMIN y SUPERVISOR
                        .requestMatchers("/reportes/**").hasAnyRole("ADMIN","SUPERVISOR")
                        // Compras y pagos-compra: ADMIN y SUPERVISOR
                        .requestMatchers("/compras/**", "/pagos-compra/**").hasAnyRole("ADMIN","SUPERVISOR")
                        // Mermas y devoluciones a proveedor: ADMIN y SUPERVISOR
                        .requestMatchers("/mermas/**", "/devoluciones-proveedor/**").hasAnyRole("ADMIN","SUPERVISOR")
                        // Caducidades: lectura para todos roles autenticados; acciones quedan protegidas por rutas POST específicas
                        .requestMatchers(HttpMethod.GET, "/caducidades/**").hasAnyRole("ADMIN","EMPLEADO","SUPERVISOR")
                        .requestMatchers(HttpMethod.POST, "/caducidades/**").hasAnyRole("ADMIN","SUPERVISOR")
                        // Fiados: visualización para ADMIN/EMPLEADO/SUPERVISOR
                        .requestMatchers("/fiados/**").hasAnyRole("ADMIN","EMPLEADO","SUPERVISOR")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { 
        return org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
