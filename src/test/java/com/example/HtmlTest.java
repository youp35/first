package com.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class HtmlTest {

    @Test
    public void testIndexContainsHello() throws Exception {
        String html = new String(Files.readAllBytes(Paths.get("index.html")));
        assertTrue(html.contains("<h1>Hello 12345</h1>"), "HTML should contain Hello 12345");
    }

    @Test
    public void testHasBodyTag() throws Exception {
        String html = new String(Files.readAllBytes(Paths.get("index.html")));
        assertTrue(html.contains("<body>"), "HTML should contain <body>");
    }
}