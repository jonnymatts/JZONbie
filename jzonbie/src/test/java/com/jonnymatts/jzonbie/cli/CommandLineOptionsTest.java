package com.jonnymatts.jzonbie.cli;

import com.jonnymatts.jzonbie.HttpsOptions;
import com.jonnymatts.jzonbie.JzonbieOptions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class CommandLineOptionsTest {

    @Test
    void noCommandLineOptions() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions();

        assertThat(commandLineOptions.zombieHeaderName).isNull();
        assertThat(commandLineOptions.httpPort).isNull();
        assertThat(commandLineOptions.httpPort).isNull();
        assertThat(commandLineOptions.httpsEnabled).isFalse();
        assertThat(commandLineOptions.httpsPort).isNull();
        assertThat(commandLineOptions.keystoreLocation).isNull();
        assertThat(commandLineOptions.keystorePassword).isNull();
        assertThat(commandLineOptions.callHistoryCapacity).isNull();
        assertThat(commandLineOptions.failedRequestsCapacity).isNull();
        assertThat(commandLineOptions.initialPrimingFile).isNull();
        assertThat(commandLineOptions.defaultPrimingFile).isNull();
    }

    @Test
    void zombieHeaderName() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--zombie-header-name", "name");

        assertThat(commandLineOptions.zombieHeaderName).isEqualTo("name");
    }

    @Test
    void zombieHeaderNameShort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("-z", "name");

        assertThat(commandLineOptions.zombieHeaderName).isEqualTo("name");
    }

    @Test
    void httpPort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--port", "8000");

        assertThat(commandLineOptions.httpPort).isEqualTo(8000);
    }

    @Test
    void httpPortShort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("-p", "8000");

        assertThat(commandLineOptions.httpPort).isEqualTo(8000);
    }

    @Test
    void httpsEnabled() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--https");

        assertThat(commandLineOptions.httpsEnabled).isTrue();
    }

    @Test
    void httpsPort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--https-port", "8000");

        assertThat(commandLineOptions.httpsPort).isEqualTo(8000);
    }

    @Test
    void keystoreLocation() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--keystore", "keystore");

        assertThat(commandLineOptions.keystoreLocation).isEqualTo("keystore");
    }

    @Test
    void keystoreLocationShort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("-k", "keystore");

        assertThat(commandLineOptions.keystoreLocation).isEqualTo("keystore");
    }

    @Test
    void keystorePassword() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--keystore-password", "password");

        assertThat(commandLineOptions.keystorePassword).isEqualTo("password");
    }

    @Test
    void keystorePasswordShort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("-kp", "password");

        assertThat(commandLineOptions.keystorePassword).isEqualTo("password");
    }

    @Test
    void commonName() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--common-name", "common-name");

        assertThat(commandLineOptions.commonName).isEqualTo("common-name");
    }

    @Test
    void commonNameShort() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("-cn", "common-name");

        assertThat(commandLineOptions.commonName).isEqualTo("common-name");
    }

    @Test
    void callHistoryCapacity() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--call-history-capacity", "100");

        assertThat(commandLineOptions.callHistoryCapacity).isEqualTo(100);
    }

    @Test
    void failedRequestsCapacity() {
        final CommandLineOptions commandLineOptions = getCommandLineOptions("--failed-requests-capacity", "100");

        assertThat(commandLineOptions.failedRequestsCapacity).isEqualTo(100);
    }

    @Test
    void initialPrimingFile() {
        CommandLineOptions commandLineOptions = getCommandLineOptions("--initial-priming-file", "missing-file");
        assertThat(commandLineOptions.initialPrimingFile).isEqualTo(new File("missing-file"));
    }

    @Test
    void defaultPrimingFile() {
        CommandLineOptions commandLineOptions = getCommandLineOptions("--default-priming-file", "missing-file");
        assertThat(commandLineOptions.defaultPrimingFile).isEqualTo(new File("missing-file"));
    }

    @Test
    void toJzonbieOptions() {
        final JzonbieOptions jzonbieOptions = CommandLineOptions.toJzonbieOptions(
                CommandLineOptions.parse(new String[]{
                                "-p", "8000",
                                "-z", "name",
                                "--https",
                                "--https-port", "8001",
                                "-k", "keystore",
                                "-kp", "password",
                                "-cn", "common-name",
                                "--call-history-capacity", "100",
                                "--failed-requests-capacity", "50",
                                "--initial-priming-file", "initial",
                                "--default-priming-file", "default",
                        }
                )
        );

        assertThat(jzonbieOptions.getHttpPort()).isEqualTo(8000);
        assertThat(jzonbieOptions.getZombieHeaderName()).isEqualTo("name");
        assertThat(jzonbieOptions.getCallHistoryCapacity()).isEqualTo(100);
        assertThat(jzonbieOptions.getFailedRequestsCapacity()).isEqualTo(50);
        assertThat(jzonbieOptions.getInitialPrimingFile()).contains(new File("initial"));
        assertThat(jzonbieOptions.getDefaultPrimingFile()).contains(new File("default"));

        final HttpsOptions httpsOptions = jzonbieOptions.getHttpsOptions().get();
        assertThat(httpsOptions.getPort()).isEqualTo(8001);
        assertThat(httpsOptions.getKeystoreLocation()).contains("keystore");
        assertThat(httpsOptions.getKeystorePassword()).contains("password");
        assertThat(httpsOptions.getCommonName()).isEqualTo("common-name");
    }

    @Test
    void toJzonbieOptionsWithNoArgs() {
        final JzonbieOptions jzonbieOptions = CommandLineOptions.toJzonbieOptions(CommandLineOptions.parse(new String[]{}));

        assertThat(jzonbieOptions.getHttpPort()).isZero();
        assertThat(jzonbieOptions.getZombieHeaderName()).isEqualTo("zombie");
        assertThat(jzonbieOptions.getHttpsOptions()).isEmpty();
        assertThat(jzonbieOptions.getCallHistoryCapacity()).isEqualTo(1000);
    }

    private CommandLineOptions getCommandLineOptions(String... args) {
        final CommandLine cmd = new CommandLine(CommandLineOptions.class);
        cmd.parseArgs(args);
        return cmd.getCommand();
    }

    private CommandLineOptions getCommandLineOptions(String key, String value) {
        return getCommandLineOptions(new String[]{key, value});
    }
}