package com.jonnymatts.jzonbie.persistence;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JzonbieFilePersistenceTest {

    private static final String FILE_CONTENT = "This is the contents of the file";
    @Test
    void canCreateFoldersAndFileInJzonbieHome() throws IOException {
        Path tempJzonbieHome = Files.createTempDirectory(".tempJzonbieHome");

        JzonbieFilePersistence underTest = new JzonbieFilePersistence(tempJzonbieHome.toString());

        File file = underTest.createFileIfNotAlreadyExists("/subFolder/file.txt");

        assertThat(file.exists()).isTrue();
    }

    @Test
    void doesNotOverwriteFileIfAlreadyCreated() throws IOException {
        //Given
        Path tempJzonbieHome = Files.createTempDirectory(".tempJzonbieHome");
        JzonbieFilePersistence underTest = new JzonbieFilePersistence(tempJzonbieHome.toString());

        File file = underTest.createFileIfNotAlreadyExists("/subFolder/file.txt");

        writeContentToFile(file, FILE_CONTENT);

        //When
        underTest.createFileIfNotAlreadyExists("/subFolder/file.txt");

        //Then
        assertThat(getFirstLineFromFile(file)).isEqualTo(FILE_CONTENT);
    }

    @Test
    void readFileInSubFolderInJzonbieHome() throws IOException {
        Path tempJzonbieHome = Files.createTempDirectory(".tempJzonbieHome");

        JzonbieFilePersistence underTest = new JzonbieFilePersistence(tempJzonbieHome.toString());

        File file = underTest.createFileIfNotAlreadyExists("/subFolder/file.txt");
        try(FileWriter writer = new FileWriter(file)) {
            writer.write("This is the contents of the file");
        }

        File foundFile = underTest.findFile("/subFolder/file.txt");
        try (final Scanner scanner = new Scanner(foundFile)) {
            assertThat(scanner.nextLine()).isEqualTo("This is the contents of the file");
        }
    }

    @Test
    void createFileIfNotAlreadyExistsThrowsJzonbiePersistenceExceptionWhenErrorOccurs() throws IOException {
        Path tempJzonbieHome = Files.createTempDirectory(".tempJzonbieHome");

        new JzonbieFilePersistence(tempJzonbieHome.toString()).createFileIfNotAlreadyExists("fakeFolder");

        assertThatThrownBy(() -> new JzonbieFilePersistence(tempJzonbieHome.toString()).createFileIfNotAlreadyExists("fakeFolder/realFile.txt"))
        .hasMessage("Could not create file")
        .isInstanceOf(JzonbiePersistenceException.class);
    }

    @Test
    void findFileThrowsJzonbiePersistenceExceptionWhenErrorOccurs() {
        assertThatThrownBy(() -> new JzonbieFilePersistence("fakePath").findFile("missingFile"))
        .hasMessage("Could not find file at fakePath/.jzonbie/missingFile")
        .isInstanceOf(JzonbiePersistenceException.class);
    }

    private void writeContentToFile(File file, String content) throws IOException {
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private String getFirstLineFromFile(File file) throws FileNotFoundException {
        try(Scanner scanner = new Scanner(file)) {
            return  scanner.nextLine();
        }
    }
}
