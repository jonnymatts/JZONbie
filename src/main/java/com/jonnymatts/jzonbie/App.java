package com.jonnymatts.jzonbie;

import static java.lang.String.format;

public class App {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        final Jzonbie jzonbie = new Jzonbie(PORT);

        System.out.println(format("Started server on port: %s", jzonbie.getPort()));
    }
}