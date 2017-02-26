package com.jonnymatts.jzonbie;

import java.util.Optional;
import java.util.function.Function;

import static com.jonnymatts.jzonbie.JzonbieOptions.options;
import static java.lang.String.format;
import static java.util.function.Function.identity;

public class App {

    public static void main(String[] args) {
        final JzonbieOptions options = options();
        getEnvironmentVariable("ZOMBIE_HEADER_NAME", identity())
                .map(options::withZombieHeaderName);

        final Integer port = getEnvironmentVariable("JZONBIE_PORT", Integer::parseInt).orElse(8080);
        options.withPort(port);

        final Jzonbie jzonbie = new Jzonbie(options);

        System.out.println(format("Started server on port: %s", jzonbie.getPort()));
    }

    private static <T> Optional<T> getEnvironmentVariable(String variableName, Function<String, T> mapper) {
        return Optional.ofNullable(System.getenv(variableName)).map(mapper);
    }
}