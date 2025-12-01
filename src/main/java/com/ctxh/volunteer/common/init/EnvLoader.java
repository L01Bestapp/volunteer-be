package com.ctxh.volunteer.common.init;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    private EnvLoader() {}

    public static void initEnv() {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}