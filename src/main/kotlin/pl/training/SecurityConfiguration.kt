package pl.training

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.authorization.AuthorizationManager
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.provisioning.JdbcUserDetailsManager
import org.springframework.security.provisioning.UserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import pl.training.security.BasicAuthenticationEntryPoint
import pl.training.security.RequestUrlAuthorizationManager
import pl.training.security.jwt.JwtAuthenticationFilter
import javax.sql.DataSource


/*AuthenticationManager authenticationManager // Interfejs/kontrakt dla procesu uwierzytelnienia użytkownika
     ProviderManager providerManager // Podstawowa implementacja AuthenticationManager, deleguje proces uwierzytelnienia do jednego z obiektów AuthenticationProvider
         AuthenticationProvider authenticationProvider // Interfejs/kontrakt dla obiektów realizujących uwierzytelnianie z wykorzystaniem konkretnego mechanizmu/implementacji
             DaoAuthenticationProvider daoAuthenticationProvider // Jedna z implementacji AuthenticationProvider, ładuje dane o użytkowniku wykorzystując UserDetailsService i porównuje je z tymi podanymi w czasie logowani
                 UserDetailsService userDetailsService // Interfejs/kontrakt usługi ładującej dane dotyczące użytkownika

 UsersDetailsManager usersDetailsManager Interfejs/kontrakt pochodny UserDetailsService, pozwalający na zarządzanie użytkownikami
     InMemoryUserDetailsManager inMemoryUserDetailsManager // Jedna z implementacji UsersDetailsManager, przechowuje informacje w pamięci

 PasswordEncoder passwordEncoder //Interfejs/kontrakt pozwalający na hashowanie i porównywanie haseł
     BCryptPasswordEncoder bCryptPasswordEncoder //Jedna z implementacji PasswordEncoder

 SecurityContextHolder securityContextHolder // Przechowuje/udostępnia SecurityContext
     SecurityContext securityContext // Kontener przechowujący Authentication
         Authentication authentication // Reprezentuje dane uwierzytelniające jak i uwierzytelnionego użytkownika/system
             UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken // Jedna z implementacji Authentication, zawiera login i hasło jako credentials
                 UserDetails userDetails // Interfejs/kontrakt opisujący użytkownika
                 GrantedAuthority grantedAuthority // Interfejs/kontrakt opisujący role/uprawnienia
                     SimpleGrantedAuthority simpleGrantedAuthority // Jedna z implementacji SimpleGrantedAuthority

 AuthorizationManager authorizationManager // Interfejs/kontrakt dla procesu autoryzacji
     AuthoritiesAuthorizationManager authoritiesAuthorizationManager // Jedna z implementacji AuthorizationManager (role)*/

// @EnableWebSecurity(debug = true)
@Configuration
class SecurityConfiguration {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /*fun defaultUser(): UserDetails = User
        .withUsername("admin")
        .password(passwordEncoder().encode("admin"))
        .roles("ADMIN")
        .authorities("read", "write")
        .build()*/

    /* @Bean
     fun userDetailsService() = UserDetailsService { username ->
         if (!username.equals("admin", true))
             throw UsernameNotFoundException("User not found")
         else
             defaultUser()
     }*/

    /*@Bean
    fun userDetailsManager(dataSource: DataSource): UserDetailsManager {
        // return InMemoryUserDetailsManager(defaultUser())
        val manager = JdbcUserDetailsManager(dataSource)
        // manager.setUsersByUsernameQuery("select username, password, enabled from users where username = ?")
        // manager.setAuthoritiesByUsernameQuery("select username, authority from authorities where username = ?")
        return manager
    }*/

    @Bean
    fun securityFilterChain(http: HttpSecurity, jwtFilter: JwtAuthenticationFilter): SecurityFilterChain {
        return http
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
            .httpBasic(withDefaults())
           /*.httpBasic {
                it
                    .realmName("training")
                    .authenticationEntryPoint(BasicAuthenticationEntryPoint())
            }*/
            //.formLogin(withDefaults())
            .formLogin {
                it
                    .loginPage("/login.html")
                //.usernameParameter("username")
                //.passwordParameter("password")
                //.successHandler(new CustomAuthenticationSuccessHandler())
                //.failureHandler(new CustomAuthenticationFailureHandler())
            }
            .logout {
                it
                    .logoutRequestMatcher(AntPathRequestMatcher("/logout.html"))
                    .logoutSuccessUrl("/")
                    .invalidateHttpSession(true)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/login.html").permitAll()
                    .requestMatchers("/api/tokens").permitAll()
                    .requestMatchers("/h2/**").hasAnyRole("ADMIN")
                    .requestMatchers(GET, "/api/users").authenticated()
                    .requestMatchers(POST, "/api/cards").hasAnyRole("USER", "ADMIN")
                    //anyRequest().access(RequestUrlAuthorizationManager())
                    .anyRequest().authenticated()
            }
            // .sessionManagement { it.disable() }
            .headers { it.frameOptions { config -> config.disable() } }
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