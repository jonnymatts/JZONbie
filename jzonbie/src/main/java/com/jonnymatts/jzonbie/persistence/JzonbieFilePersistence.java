package com.jonnymatts.jzonbie.persistence;


import java.io.File;

import static java.lang.String.format;
import static java.nio.file.Paths.get;

public class JzonbieFilePersistence {

    private static final String JZONBIE_HOME_FOLDER_NAME = ".jzonbie";
    private final File jzonbieHomePath;

    public JzonbieFilePersistence(String jzonbieHomePath) {
        this.jzonbieHomePath = get(jzonbieHomePath, JZONBIE_HOME_FOLDER_NAME).toFile();
        this.jzonbieHomePath.mkdirs();
    }

    public File createFileIfNotAlreadyExists(String subPathIncludingFileName) {
        File file = get(jzonbieHomePath.getPath(), subPathIncludingFileName).toFile();

        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
            return file;
        } catch (Exception e) {
            throw new JzonbiePersistenceException("Could not create file", e);
        }
    }

    public File findFile(String subPathToFile) {
        final File file = get(jzonbieHomePath.getPath(), subPathToFile).toFile();
        if(file.exists()) {
            return file;
        } else {
            throw new JzonbiePersistenceException(format("Could not find file at %s", file.getPath()));
        }
    }
}
