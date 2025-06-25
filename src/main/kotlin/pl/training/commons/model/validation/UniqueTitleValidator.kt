package pl.training.commons.model.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import pl.training.blog.application.output.ArticleRepository

class UniqueTitleValidator(
    private val articleRepository: ArticleRepository
) : ConstraintValidator<UniqueTitle, String> {
    override fun isValid(title: String, context: ConstraintValidatorContext?) =
        !articleRepository.existsByTitle(title)
}
