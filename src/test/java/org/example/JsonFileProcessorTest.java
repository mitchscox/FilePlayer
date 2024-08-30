package org.example;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


    public class JsonFileProcessorTest {

        @Test
        public void testFileExists() {
            // Specify the directory and file name
            Path filePath = Paths.get("/home/bugeye2/IdeaProjects/FilePlayer/input/mitch.json");

            // Check if the file exists
            assertTrue(Files.exists(filePath), "The file should exist in the specified directory.");
        }

        @Test
        public void testFileDoesNotExist() {
            // Specify the directory and file name
            Path filePath = Paths.get("/home/bugeye2/IdeaProjects/FilePlayer/output/mitch.json");

            // Check if the file does not exist
            assertFalse(Files.exists(filePath), "The file should not exist in the specified directory.");
        }
    }
