package pl.training
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import pl.training.blog.application.ArticleTemplate
import pl.training.blog.application.input.ArticleAuthorActions
import pl.training.blog.application.input.ArticleSearch
import pl.training.blog.domain.ArticleCategory.IT

@SpringBootApplication
class Application(
    private val authorActions: ArticleAuthorActions,
    private val search: ArticleSearch,) :
    ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        val article = ArticleTemplate("Test", "Jan Kowalski", "", IT)
        val id = authorActions.create(article)
        println(search.findByUid(id))
        //println(search.findByUid(id))
    }

}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
