package pl.training

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.client.RestTemplate
import org.springframework.web.cors.CorsConfiguration
import pl.training.security.DefaultGrantedAuthoritiesMapper
import pl.training.security.KeycloakGrantedAuthoritiesMapper
import pl.training.security.KeycloakJwtGrantedAuthoritiesConverter
import pl.training.security.KeycloakLogoutHandler

/*
Definiowanie First login flow dla logowania przez GitHub
Authentication -> Create flow (detect existing user flow, Basic flow) -> Add step
(Detect existing broker user, Automatically set existing user)
Identity providers -> github -> First login flow -> detect existing user flow
 */
@Configuration
class SecurityConfiguration {


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .oauth2ResourceServer { it.jwt(::jwtConfig) }
            .oauth2Login { it.userInfoEndpoint(::userInfoCustomizer) }
            .authorizeHttpRequests {
                it
                    .anyRequest().hasRole("ADMIN")
            }
            .logout {
                it
                    .logoutRequestMatcher(AntPathRequestMatcher("/logout"))
                    .logoutSuccessUrl("/")
                    //.invalidateHttpSession(true)
                    .addLogoutHandler(KeycloakLogoutHandler(RestTemplate()))
            }
            .cors { it.configurationSource { request -> corsConfiguration() } }
            .csrf { it.ignoringRequestMatchers("/api/**") }
            .build()
    }


    private fun jwtConfig(jwtConfigurer: OAuth2ResourceServerConfigurer<HttpSecurity>.JwtConfigurer) {
        val jwtConverter = JwtAuthenticationConverter()
        jwtConverter.setJwtGrantedAuthoritiesConverter(KeycloakJwtGrantedAuthoritiesConverter())
        jwtConfigurer.jwtAuthenticationConverter(jwtConverter)
    }

    /* @Bean
   fun jwtConfigurer(): JwtAuthenticationConverter {
       val jwtConverter = JwtAuthenticationConverter()
       jwtConverter.setJwtGrantedAuthoritiesConverter(KeycloakJwtGrantedAuthoritiesConverter())
       return jwtConverter
   }*/


    // Client scopes -> Client scope details (roles) -> Mapper details -> Add to userinfo enabled (Keycloak Admin console)
    fun userInfoCustomizer(userInfoEndpointConfig: OAuth2LoginConfigurer<HttpSecurity>.UserInfoEndpointConfig) {
        userInfoEndpointConfig.userAuthoritiesMapper(DefaultGrantedAuthoritiesMapper())
    }

    @Bean
    fun corsConfiguration() = CorsConfiguration().apply {
        allowedOrigins = listOf("http://localhost:8080")
        allowedHeaders = listOf("*")
        allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
        allowCredentials = true
    }

}