package pl.training

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.authorization.AuthorizationManager
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

    @Bean
    fun userDetailsManager(dataSource: DataSource): UserDetailsManager {
        // return InMemoryUserDetailsManager(defaultUser())
        val manager = JdbcUserDetailsManager(dataSource)
        // manager.setUsersByUsernameQuery("select username, password, enabled from users where username = ?")
        // manager.setAuthoritiesByUsernameQuery("select username, authority from authorities where username = ?")
        return manager
    }

}