package pl.training

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import kotlin.apply

// @EnableR2dbcRepositories
@Configuration
class DatabaseConfiguration {

    @Bean
    fun databaseInitializer(connectionFactory: ConnectionFactory) = ConnectionFactoryInitializer()
        .apply {
            setConnectionFactory(connectionFactory)
            setDatabasePopulator(CompositeDatabasePopulator().apply {
                addPopulators(
                    ResourceDatabasePopulator(ClassPathResource("schema.sql"), ClassPathResource("data.sql"))
                )
            })
        }

}