package pl.training

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application(private val userService: UserService) : ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        runBlocking {
            userService.getAll()
                .collect { user -> println(user.toString()) }
            delay(5_000)
        }
    }

}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
