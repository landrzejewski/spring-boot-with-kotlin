package pl.training.commons.converters

import org.springframework.core.convert.converter.Converter
import java.time.ZonedDateTime
import java.util.*

class ZonedDateTimeWriteConverter : Converter<ZonedDateTime, Date> {
    override fun convert(zonedDateTime: ZonedDateTime) = Date.from(zonedDateTime.toInstant())
}
