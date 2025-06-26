package pl.training

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
class SecurityConfiguration {


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeHttpRequests {
                it
                    .anyRequest().authenticated()
            }
            .cors { it.configurationSource { request -> corsConfiguration() } }
            .csrf { it.ignoringRequestMatchers("/api/**") }
            .build()
    }

    @Bean
    fun corsConfiguration() = CorsConfiguration().apply {
        allowedOrigins = listOf("http://localhost:8080")
        allowedHeaders = listOf("*")
        allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE")
        allowCredentials = true
    }

}