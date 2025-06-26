package pl.training

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import pl.training.security.KeycloakJwtGrantedAuthoritiesConverter

@Configuration
class SecurityConfiguration {


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .oauth2ResourceServer { it.jwt(::jwtConfig) }
            .authorizeHttpRequests {
                it
                    .anyRequest().hasRole("ADMIN")
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

    @Bean
    fun corsConfiguration() = CorsConfiguration().apply {
        allowedOrigins = listOf("http://localhost:8080")
        allowedHeaders = listOf("*")
        allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
        allowCredentials = true
    }

}