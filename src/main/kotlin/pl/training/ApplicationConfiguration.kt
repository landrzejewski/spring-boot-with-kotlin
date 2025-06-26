package pl.training

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import pl.training.blog.application.ArticleAuthorActionsService
import pl.training.blog.application.ArticleReaderActionsService
import pl.training.blog.application.ArticleSearchService
import pl.training.blog.application.output.ArticleEventEmitter
import pl.training.blog.application.output.ArticleRepository
import pl.training.security.ApiKeyAuthenticationProvider

@Configuration
class ApplicationConfiguration {

    @Bean
    fun articleAuthorActions(articleRepository: ArticleRepository) =
        ArticleAuthorActionsService(articleRepository)

    @Bean
    fun articleReaderActions(articleRepository: ArticleRepository, articleEventEmitter: ArticleEventEmitter) =
        ArticleReaderActionsService(articleRepository, articleEventEmitter)

    @Bean
    fun articleSearch(articleRepository: ArticleRepository) =
        ArticleSearchService(articleRepository)

    @Autowired
    fun configure(
        builder: AuthenticationManagerBuilder,
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ) {
        var daoAuthenticationProvider = DaoAuthenticationProvider()
        daoAuthenticationProvider.setUserDetailsService(userDetailsService)
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder)
        builder.authenticationProvider(daoAuthenticationProvider)

        builder.authenticationProvider(ApiKeyAuthenticationProvider())
    }

}