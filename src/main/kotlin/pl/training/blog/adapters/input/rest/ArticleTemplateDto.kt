package pl.training.blog.adapters.input.rest

import jakarta.validation.constraints.Pattern
import pl.training.commons.model.validation.UniqueTitle

class ArticleTemplateDto(
    @field:Pattern(regexp = "\\w{3,}")
    @UniqueTitle
    val title: String,
    val author: String,
    val content: String,
    val category: String
)
