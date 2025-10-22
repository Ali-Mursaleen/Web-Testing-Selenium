package com.Abdul_Raqeeb.selenium;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class TestUtils {
    private static final Path CREDENTIALS = Path.of("test-credentials.txt");

    public static void saveCredentials(String username, String password) {
        try {
            String line = username + ":" + password;
            Files.writeString(CREDENTIALS, line, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Saved credentials to " + CREDENTIALS.toAbsolutePath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String[] readCredentials() {
        try {
            if (!Files.exists(CREDENTIALS)) return null;
            String s = Files.readString(CREDENTIALS).trim();
            if (s.isEmpty()) return null;
            String[] parts = s.split(":", 2);
            if (parts.length < 2) return null;
            return new String[]{parts[0], parts[1]};
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
