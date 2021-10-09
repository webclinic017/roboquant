@file:Suppress("unused")
package org.roboquant.common

import java.util.logging.*

/**
 * Simple Logging object that provides utility methods to create and update loggers. Where many loggers APIs are
 * focused on serverside applications, this API is also suitable for interactive environments like notebooks.
 *
 * Features:
 *
 * - Smaller and more user-friendly log messages
 * - Can set log levels at runtime
 * - Use color syntax
 *
 * Please note this is a logger that can is used in Kotlin source code, not to be confused with a MetricsLogger
 * that can be used to log metrics during a run.
 */
object Logging {

    private var defaultLevel = Level.INFO

    class LoggingFormatter : SimpleFormatter() {

        companion object {

            // ANSI escape code
            const val ANSI_RESET = "\u001B[0m"
            const val ANSI_BLACK = "\u001B[30m"
            const val ANSI_RED = "\u001B[31m"
            const val ANSI_GREEN = "\u001B[32m"
            const val ANSI_YELLOW = "\u001B[33m"
            const val ANSI_BLUE = "\u001B[34m"
            const val ANSI_PURPLE = "\u001B[35m"
            const val ANSI_CYAN = "\u001B[36m"
            const val ANSI_WHITE = "\u001B[37m"

            fun blue(msg: Any) = "$ANSI_BLUE$msg$ANSI_RESET"
            fun green(msg: Any) = "$ANSI_GREEN$msg$ANSI_RESET"
        }

        override fun format(lr: LogRecord): String {
            return "[$ANSI_BLUE${lr.level.localizedName}$ANSI_RESET] $ANSI_GREEN${lr.loggerName}:$ANSI_RESET ${lr.message}\n"
        }
    }


    init {
        // Install a modified formatter
        val handler = ConsoleHandler()
        handler.level = Level.FINEST
        handler.formatter = LoggingFormatter()
        resetHandler(handler)
    }

    fun resetHandler(handler: Handler) {
        LogManager.getLogManager().reset()
        val rootLogger = Logger.getLogger("")
        rootLogger.addHandler(handler)
    }

    fun getLogger(clazz: Class<*>): Logger {
        return getLogger(clazz.simpleName)
    }

    fun getLogger(obj: Any): Logger {
        return getLogger(obj.javaClass.simpleName)
    }

    fun getLogger(name: String): Logger {
        val mainLogger: Logger = Logger.getLogger(name)
        mainLogger.level = defaultLevel
        return mainLogger
    }

    /**
     * Set the logging level for all loggers to specified [level] and optionally restrict
     * update to loggers whose name start with provided [prefix].
     */
    fun setLevel(level: Level, prefix: String = "", updateDefault: Boolean = true) {
        val manager = LogManager.getLogManager()
        if (updateDefault) defaultLevel = level
        LogManager.getLogManager().loggerNames.toList().forEach {
            if (it.startsWith(prefix)) manager.getLogger(it).level = level
        }
    }

    /**
     * Set the default logging level for new Loggers. This won't change logging level of already created loggers,
     * for that please use [setLevel].
     *
     * @param level
     */
    fun setDefaultLevel(level: Level) {
        defaultLevel = level
    }

    fun getLoggerNames() = LogManager.getLogManager().loggerNames.toList()

}
