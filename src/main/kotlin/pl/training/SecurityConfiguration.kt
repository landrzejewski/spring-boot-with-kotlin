package pl.training

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import pl.training.security.ApiKeyAuthenticationFilter

@Configuration
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(
        httpSecurity: HttpSecurity,
        apiKeyAuthenticationFilter: ApiKeyAuthenticationFilter
    ): SecurityFilterChain {
        return httpSecurity
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .httpBasic(withDefaults())
            .formLogin(withDefaults())
            .csrf { it.disable() }
            .authorizeHttpRequests { config -> config
                .requestMatchers(GET, "/articles/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(GET, "/categories/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().hasRole("ADMIN")
            }
            .build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

}
