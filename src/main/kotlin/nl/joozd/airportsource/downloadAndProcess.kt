package nl.joozd.airportsource

import nl.joozd.airportsource.args.Args
import nl.joozd.airportsource.args.Sources
import nl.joozd.airportsource.ourairports.OurAirportsProcessor

fun downloadAndProcess(args: Args){
    val processor: Processor = when(args.source){
        Sources.OUR_AIRPORTS -> OurAirportsProcessor()
    }

    processor.downloadAndProcess(args)
}