package edu.upenn.cis.cis455.storage;

import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.DatabaseException;
import edu.upenn.cis.cis455.model.UserData;
import edu.upenn.cis.cis455.model.UserKey;

import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class BerkeleyStorageImpl implements StorageInterface {

    private BerkeleyDatabase db;
    private BerkeleyViews views;
    private TransactionRunner runner;

    public BerkeleyStorageImpl(String homeDir) throws DatabaseException, FileNotFoundException {
        db = new BerkeleyDatabase(homeDir);
        views = new BerkeleyViews(db);
        runner = new TransactionRunner(db.getEnvironment());
    }

    @Override
    public int getCorpusSize() {
        return 0;
    }

    @Override
    public int addDocument(String url, String documentContents) {
        return 0;
    }

    @Override
    public int getLexiconSize() {
        return 0;
    }

    @Override
    public int addOrGetKeywordId(String keyword) {
        return 0;
    }

    @Override
    public int addUser(String username, String firstName, String lastName, String password) {
        try {
            TransactionAddUser transaction = new TransactionAddUser(username, firstName, lastName, encrypt(password));
            runner.run(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
    }

    @Override
    public boolean getSessionForUser(String username, String password) {
        try {
            TransactionAuthenticateUser transaction = new TransactionAuthenticateUser(username, encrypt(password));
            runner.run(transaction);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String getDocument(String url) {
        return null;
    }

    @Override
    public void close() { db.close(); }

    /******************************
     * PRIVATE: USER Transactions *
     ******************************/

    /**
     * Add a user
     * @throw UserAlreadyExistException the same username is found in the DB
     */
    private class TransactionAddUser implements TransactionWorker {
        private String username;
        private String firstName;
        private String lastName;
        private String password;

        public TransactionAddUser(String username, String firstName, String lastName, String password) {
            this.username = username; this.firstName = firstName; this.lastName = lastName; this.password = password;
        }

        @Override
        public void doWork() throws Exception {
            Map users = views.getUserMap();
            if (users.containsKey(new UserKey(username))) {
                throw new UserAlreadyExistException();
            } else {
                users.put(new UserKey(username), new UserData(firstName, lastName, password));
            }
        }
    }

    /**
     * Authenticate a User
     * @throw PasswordMismatchException if passwords do not match.
     * @throw NoSuchUserException if user does not exist.
     * */
    private class TransactionAuthenticateUser implements TransactionWorker {
        private String username, password;

        public TransactionAuthenticateUser(String username, String password) {
            this.username = username; this.password = password;
        }

        @Override
        public void doWork() throws Exception {
            Map users = views.getUserMap();
            UserData data = (UserData) users.get(new UserKey(username));
            if (data == null) {
                throw new NoSuchUserException();
            } else if (!data.getPassword_hash().equals(password)) {
                throw new PasswordMismatchException();
            }
        }
    }

    /****************************************
     * PRIVATE: User Transaction Exceptions *
     ****************************************/

    private class NoSuchUserException extends Exception {
        @Override
        public String getMessage() { return "User does not exist!"; }
    }
    private class PasswordMismatchException extends Exception {
        @Override
        public String getMessage() { return "Password Mismatch!"; }
    }
    private class UserAlreadyExistException extends Exception {
        @Override
        public String getMessage() { return "User Already Exist!"; }
    }

    /*************************************
     * PRIVATE: SHA-256 Hashing Function *
     *************************************/

    private String encrypt(String rawPassword) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(rawPassword.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md.digest();
        return String.format("%064x", new BigInteger(1, digest));
    }
}
