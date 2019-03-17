package edu.upenn.cis.cis455.storage;

import com.sleepycat.je.DatabaseException;

import java.io.FileNotFoundException;

public class StorageFactory {
    public static StorageInterface getDatabaseInstance(String directory) throws FileNotFoundException, DatabaseException {
        return new BerkeleyStorageImpl(directory);
    }
}
