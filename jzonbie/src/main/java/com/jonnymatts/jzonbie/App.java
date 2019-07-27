package com.jonnymatts.jzonbie;

import com.jonnymatts.jzonbie.cli.CommandLineOptions;
import picocli.CommandLine;

public class App {

    public static void main(String[] args) {
        final CommandLineOptions commandLineOptions = CommandLineOptions.parse(args);

        if(commandLineOptions.usageHelpRequested) {
            CommandLine.usage(commandLineOptions, System.out);
            System.exit(0);
        }

        final JzonbieOptions jzonbieOptions = CommandLineOptions.toJzonbieOptions(commandLineOptions);

        new Jzonbie(jzonbieOptions);
    }
}