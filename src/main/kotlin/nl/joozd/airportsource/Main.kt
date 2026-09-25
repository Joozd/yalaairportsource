package nl.joozd.airportsource

import nl.joozd.airportsource.args.Args
import nl.joozd.airportsource.args.buildArgsFromCli
import org.slf4j.LoggerFactory
import kotlin.io.path.createDirectory
import kotlin.io.path.exists


private val logger = LoggerFactory.getLogger("AirportSourceMain")

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(params: Array<String>) {
    try {
        val args = buildArgsFromCli(params)

        checkIfDirsExist(args)

        if (args.help) {
            printHelp()
            return
        }

        downloadAndProcess(args)
    }catch (e: Throwable) {
        logger.error("Uncaught exception occurred:\n" + e.stackTraceToString())
    }
}

private fun checkIfDirsExist(args: Args) {
    if(!args.outputDir.exists())
        args.outputDir.createDirectory()
}