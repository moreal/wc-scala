//> using scala 3.7.0
//> using toolkit 0.7.0
//> using dep com.github.alexarchambault::case-app:2.1.0-M30
//> using option -language:strictEquality

import scala.io.StdIn.readLine

import caseapp._

case class Options(
    `-l`: Boolean = false,
    `-w`: Boolean = false,
    `-c`: Boolean = false,
    `-m`: Boolean = false,
    // FIXME: `-L` option cannot be parsed.
    `-L`: Boolean = false,
)

case class Stats(
    lineCount: Int,
    wordCount: Int,
    bytesCount: Int,
    charCount: Int,
    maxLineBytesLength: Int
)

def getStats(iterator: Iterator[Char]): Stats = {
    case class State(
        lineCount: Int = 0,
        wordCount: Int = 0,
        bytesCount: Int = 0,
        charCount: Int = 0,
        maxLineBytesLength: Int = 0,
        currentLineBytes: Int = 0,
        prevChar: Option[Char] = None
    )

    val finalState = iterator.foldLeft(State()) { (state, char) =>
        val charBytes = char.toString.getBytes.length
        val isNewLine = char == '\n'
        val isWhitespace = char.isWhitespace
        
        val newLineCount = if (isNewLine) state.lineCount + 1 else state.lineCount
        val newMaxLineBytes = if (isNewLine) 
            math.max(state.maxLineBytesLength, state.currentLineBytes)
        else 
            state.maxLineBytesLength
        val newCurrentLineBytes = if (isNewLine) 0 else state.currentLineBytes + charBytes
        
        val newWordCount = state.prevChar match {
            case Some(prev) if !prev.isWhitespace && isWhitespace => state.wordCount + 1
            case _ => state.wordCount
        }
        
        State(
            lineCount = newLineCount,
            wordCount = newWordCount,
            bytesCount = state.bytesCount + charBytes,
            charCount = state.charCount + 1,
            maxLineBytesLength = newMaxLineBytes,
            currentLineBytes = newCurrentLineBytes,
            prevChar = Some(char)
        )
    }
    
    // Handle last line and word
    val adjustedMaxLineBytes = math.max(finalState.maxLineBytesLength, finalState.currentLineBytes)
    val adjustedWordCount = finalState.wordCount + finalState.prevChar.map(c => if (!c.isWhitespace) 1 else 0).getOrElse(0)
    Stats(
        lineCount = finalState.lineCount,
        wordCount = adjustedWordCount,
        bytesCount = finalState.bytesCount,
        charCount = finalState.charCount,
        maxLineBytesLength = adjustedMaxLineBytes
    )
}

@main
def main(args: String*): Unit =
    // (Either[Error, T], Boolean, Boolean, RemainingArgs)
    val (options, remaining) = CaseApp.detailedParseWithHelp[Options](args) match
        case Left(error) => 
            println(error)
            sys.exit(1)
        case Right(Left(err), helpAsked, usageAsked, remaining) =>
            println("Error:" + err.message)
            sys.exit(1)
        case Right(Right(options), helpAsked, usageAsked, remaining) =>
            (options, remaining)

    val defaultPrintLineCount = true
    val defaultPrintWordCount = true
    val defaultPrintBytesCount = true
    val defaultPrintCharCount = false
    val defaultPrintMaxLineBytesLength = false

    val useDefaults = !(options.`-l` || options.`-w` || options.`-c` || options.`-m` || options.`-L`)
    val printLineCount = if (useDefaults) defaultPrintLineCount else options.`-l`
    val printWordCount = if (useDefaults) defaultPrintWordCount else options.`-w`
    val printBytesCount = if (useDefaults) defaultPrintBytesCount else options.`-c`
    val printCharCount = if (useDefaults) defaultPrintCharCount else options.`-m`
    val printMaxLineBytesLength = if (useDefaults) defaultPrintMaxLineBytesLength else options.`-L`

    if (printCharCount && printBytesCount) {
        println("Error: -c and -m options cannot be used together.")
        sys.exit(1)
    }

    var lineCount = 0
    var wordCount = 0
    var bytesCount = 0
    var charCount = 0
    var maxLineBytesLength = 0

    val stats = getStats(scala.io.Source.stdin)

    if (printLineCount) print(f"${stats.lineCount}%8d")
    if (printWordCount) print(f"${stats.wordCount}%8d")
    if (printBytesCount) print(f"${stats.bytesCount}%8d")
    if (printCharCount) print(f"${stats.charCount}%8d")
    if (printMaxLineBytesLength) print(f"${stats.maxLineBytesLength}%8d")

    println()
