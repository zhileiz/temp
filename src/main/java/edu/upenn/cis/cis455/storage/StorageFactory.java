package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.DatabaseException;

import java.io.FileNotFoundException;

public class StorageFactory {

    private static StorageInterface instance;

    public static String DB_NAME;

    public static StorageInterface getDatabaseInstance(String directory) throws FileNotFoundException, DatabaseException {
        if (instance == null) {
            DB_NAME = directory;
            instance = new BerkeleyStorageImpl(directory);
        }
        return instance;
    }
}
