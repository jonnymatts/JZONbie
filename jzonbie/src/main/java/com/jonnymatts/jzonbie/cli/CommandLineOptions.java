package com.jonnymatts.jzonbie.cli;

import com.jonnymatts.jzonbie.HttpsOptions;
import com.jonnymatts.jzonbie.JzonbieOptions;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;

import static com.jonnymatts.jzonbie.HttpsOptions.httpsOptions;
import static com.jonnymatts.jzonbie.JzonbieOptions.options;

public class CommandLineOptions {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
    public boolean usageHelpRequested;

    @Option(names = {"-p", "--port"}, paramLabel = "PORT", description = "port the HTTP server will listen on")
    public Integer httpPort;

    @Option(names = {"-z", "--zombie-header-name"}, paramLabel = "NAME", description = "name of the HTTP header used to drive Jzonbie functions")
    public String zombieHeaderName;

    @Option(names = {"--https"}, description = "enable HTTPS")
    public boolean httpsEnabled;

    @Option(names = {"--https-port"}, paramLabel = "PORT", description = "port the HTTPS server will listen on")
    public Integer httpsPort;

    @Option(names = {"-k", "--keystore"}, paramLabel = "PATH", description = "path to keystore")
    public String keystoreLocation;

    @Option(names = {"-kp", "--keystore-password"}, paramLabel = "PASSWORD", description = "password to supplied keystore")
    public String keystorePassword;

    @Option(names = {"-cn", "--common-name"}, paramLabel = "NAME", description = "common name of generated HTTPS certificate")
    public String commonName;

    @Option(names = {"--call-history-capacity"}, paramLabel = "SIZE", description = "maximum capacity of the stored call history")
    public Integer callHistoryCapacity;

    @Option(names = {"--failed-requests-capacity"}, paramLabel = "SIZE", description = "maximum capacity of the stored failed requests")
    public Integer failedRequestsCapacity;

    @Option(names = {"--initial-priming-file"}, paramLabel = "PATH", description = "path to initial priming file JSON")
    public File initialPrimingFile;

    @Option(names = {"--default-priming-file"}, paramLabel = "PATH", description = "path to default priming file JSON")
    public File defaultPrimingFile;

    public static CommandLineOptions parse(String[] args) {
        final CommandLine cmd = new CommandLine(CommandLineOptions.class);
        cmd.parseArgs(args);
        return cmd.getCommand();
    }

    public static JzonbieOptions toJzonbieOptions(CommandLineOptions commandLineOptions) {
        final JzonbieOptions options = options();
        if(commandLineOptions.httpPort != null) {
            options.withHttpPort(commandLineOptions.httpPort);
        }
        if(commandLineOptions.zombieHeaderName != null) {
            options.withZombieHeaderName(commandLineOptions.zombieHeaderName);
        }
        if(commandLineOptions.callHistoryCapacity != null) {
            options.withCallHistoryCapacity(commandLineOptions.callHistoryCapacity);
        }
        if(commandLineOptions.failedRequestsCapacity != null) {
            options.withFailedRequestsCapacity(commandLineOptions.failedRequestsCapacity);
        }
        if(commandLineOptions.httpsEnabled) {
            final HttpsOptions httpsOptions = httpsOptions();
            if(commandLineOptions.httpsPort != null){
                httpsOptions.withPort(commandLineOptions.httpsPort);
            }
            if(commandLineOptions.keystoreLocation != null){
                httpsOptions.withKeystoreLocation(commandLineOptions.keystoreLocation);
            }
            if(commandLineOptions.keystorePassword != null){
                httpsOptions.withKeystorePassword(commandLineOptions.keystorePassword);
            }
            if(commandLineOptions.commonName != null){
                httpsOptions.withCommonName(commandLineOptions.commonName);
            }
            options.withHttps(httpsOptions);
        }
        if (commandLineOptions.initialPrimingFile != null) {
            options.withInitialPrimingFile(commandLineOptions.initialPrimingFile);
        }
        if (commandLineOptions.defaultPrimingFile != null) {
            options.withDefaultPrimingFile(commandLineOptions.defaultPrimingFile);
        }
        return options;
    }
}