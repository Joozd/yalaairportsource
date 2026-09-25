package nl.joozd.airportsource

import nl.joozd.airportsource.args.buildArgsFromCli

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(params: Array<String>) {
    val args = buildArgsFromCli(params)

    if(args.help) {
        printHelp()
        return
    }

    println("Output directory: ${args.outputDir}")
}