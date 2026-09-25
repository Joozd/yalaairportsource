package nl.joozd.airportsource

import nl.joozd.airportsource.args.Args

interface Processor {
    fun downloadAndProcess(args: Args)
}