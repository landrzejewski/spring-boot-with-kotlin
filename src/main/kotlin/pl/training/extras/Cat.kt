package pl.training.extras

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class Options(
    val numberAll: Boolean = false,
    val numberNonEmpty: Boolean = false,
    val squeezeBlank: Boolean = false
)

fun parseArgs(args: Array<String>): Pair<Options, Set<String>> {
    val flags = mutableSetOf<Char>()
    val files = mutableSetOf<String>()

    args.forEach { arg ->
        when {
            arg == "-" -> files += arg
            arg.startsWith("-") -> flags += arg.drop(1).toSet()
            else -> files += arg
        }
    }

    val options = Options(
        numberAll = 'n' in flags && 'b' !in flags,
        numberNonEmpty = 'b' in flags,
        squeezeBlank = 's' in flags
    )

    return options to files
}

fun BufferedReader.processLines(options: Options) {
    var lineNumber = 1
    var lastWasEmpty = false

    lineSequence().forEach { line ->
        val isBlank = line.isBlank()
        if (options.squeezeBlank && isBlank && lastWasEmpty) return@forEach

        val prefix = when {
            options.numberNonEmpty && !isBlank -> "%6d\t".format(lineNumber++)
            options.numberAll -> "%6d\t".format(lineNumber++)
            else -> ""
        }

        println(prefix + line)
        lastWasEmpty = isBlank
    }
}

fun readSource(file: String): BufferedReader =
    if (file == "-") BufferedReader(InputStreamReader(System.`in`))
    else File(file).bufferedReader()

fun main(args: Array<String>) {
    val (options, files) = parseArgs(args)
    val sources = files.ifEmpty { listOf("-") }

    sources.forEach { file ->
        runCatching {
            readSource(file).use { it.processLines(options) }
        }.onFailure { e ->
            System.err.println("Błąd czytania '$file': ${e.message}")
        }
    }
}
