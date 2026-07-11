package duoc.sumativa.transportes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Este es el "guardia de seguridad" principal de nuestra aplicación.
     * Aquí definimos las reglas de quién puede entrar y a qué lugares.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Apagamos la protección CSRF. 
            // Como esta aplicación es una API y no una página web tradicional con formularios, 
            // no necesitamos esta protección.
            .csrf(csrf -> csrf.disable()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. Aquí definimos las reglas de los permisos (quién entra a dónde)
            .authorizeHttpRequests(authz -> authz
                
                // REGLA 1: Para descargar guías.
                // Si alguien intenta entrar a la ruta "/api/transportes/guias/descargar", 
                // le exigimos que en su token tenga el permiso (rol) de "Guia.Descargar" estrictamente.
                .requestMatchers(HttpMethod.GET, "/api/transportes/guias/descargar").hasAuthority("ROLE_Guia.Descargar")
                
                // 2. Endpoints protegidos para los Administradores (Crear, Modificar, Eliminar, Consultar)
                .requestMatchers("/api/transportes/guias/subir", 
                                 "/api/transportes/guias/actualizar",
                                 "/api/transportes/guias/eliminar",
                                 "/api/transportes/guias/buscar",
                                 "/api/transportes/guias/listar",
                                 "/api/transportes/pedidos")
                    .hasAuthority("ROLE_Guia.Admin")
                
                // 3. Permitir acceso público a Spring Boot Actuator para monitoreo
                .requestMatchers("/actuator/**").permitAll()
                
                // 4. Cualquier otra solicitud debe estar autenticada
                .anyRequest().authenticated()
            )
            
            // 4. Le decimos a nuestro sistema que funcionará recibiendo Tokens (tipo JWT).
            // Además, le pasamos nuestro "traductor" personalizado que configuramos más abajo, 
            // el cual sabe cómo leer los roles que vienen desde Azure B2C.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        // Finalmente, armamos todas las reglas y se las pasamos a Spring Boot.
        return http.build();
    }

 

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // En Azure AD B2C los App Roles generalmente vienen en el claim "roles" o "extension_roles".
        // En Azure AD B2C los atributos personalizados viajan con el prefijo extension_
        grantedAuthoritiesConverter.setAuthoritiesClaimName("extension_Role");
        
        // Spring Boot está acostumbrado a que todos los roles empiecen con la palabra "ROLE_".
        // Así que le decimos al traductor que le pegue ese texto al principio (ej: Guia.Admin pasará a ser ROLE_Guia.Admin).
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

}
