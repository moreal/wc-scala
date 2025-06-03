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
    var lineCount = 0
    var wordCount = 0
    var bytesCount = 0
    var charCount = 0
    var maxLineBytesLength = 0

    var currentLine = new StringBuilder
    var prevChar: Option[Char] = None

    for (char <- iterator) {
        charCount += 1
        bytesCount += char.toString.getBytes.length
        
        if (char == '\n') {
            lineCount += 1
            val lineBytes = currentLine.toString.getBytes.length
            maxLineBytesLength = math.max(maxLineBytesLength, lineBytes)
            currentLine.clear()
        } else {
            currentLine.append(char)
        }
        
        // Count words based on whitespace transitions
        val isWhitespace = char.isWhitespace
        prevChar match {
            case Some(prev) if !prev.isWhitespace && isWhitespace =>
                wordCount += 1
            case _ =>
        }
        
        prevChar = Some(char)
    }
    
    // Handle last line if it doesn't end with newline
    if (currentLine.nonEmpty) {
        val lineBytes = currentLine.toString.getBytes.length
        maxLineBytesLength = math.max(maxLineBytesLength, lineBytes)
    }
    
    // Count last word if input ends with non-whitespace
    prevChar match {
        case Some(char) if !char.isWhitespace =>
            wordCount += 1
        case _ =>
    }

    Stats(lineCount, wordCount, bytesCount, charCount, maxLineBytesLength)
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
