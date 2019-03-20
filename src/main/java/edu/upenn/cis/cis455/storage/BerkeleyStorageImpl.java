package edu.upenn.cis.cis455.storage;

import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.DatabaseException;
import edu.upenn.cis.cis455.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.upenn.cis.cis455.commonUtil.CommonUtil.*;

public class BerkeleyStorageImpl implements StorageInterface {

    Logger logger = LogManager.getLogger(BerkeleyStorageImpl.class);

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
        try {
            runner.run(() -> {
                throw new CountDocumentResponse(views.getDocumentMap().size());
            });
        } catch (Exception res) {
            if (res instanceof CountDocumentResponse) {
                return ((CountDocumentResponse) res).count;
            }
        }
        return -1;
    }

    @Override
    public int addDocument(String url, String documentContents, String date) {
        try {
            TransactionAddDocument transaction = new TransactionAddDocument(url, documentContents, date);
            runner.run(transaction);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 1;
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
            logger.debug(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Object getDocument(String url) {
        try {
            TransactionGetDocument transaction = new TransactionGetDocument(url);
            runner.run(transaction);
        } catch (Exception res) {
            if (res instanceof GetDocumentResponse) {
                return ((GetDocumentResponse) res).doc;
            }
        }
        return null;
    }

    @Override
    public List<String> getAllDocumentURLs() {
        List<String> list = new ArrayList<>();
        try {
            runner.run(() -> {
                Iterator it = views.getDocumentEntrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    list.add(((DocumentKey)entry.getKey()).getUrl());
                }
            });
        } catch (Exception e) {
            logger.debug(e);
        }
        return list;
    }

    @Override
    public void close() { db.close(); }

    /**********************************
     * PRIVATE: DOCUMENT Transactions *
     **********************************/

    private class TransactionAddDocument implements TransactionWorker {
        private String url, content, date;

        public TransactionAddDocument(String url, String content, String date) {
            this.url = url; this.content = content; this.date = date;
        }

        @Override
        public void doWork() throws Exception {
            Map documents = views.getDocumentMap();
            Map contentSeen = views.getContentSeenMap();
            DocumentKey docKey = new DocumentKey(url);
            if (documents.containsKey(docKey)) {
                DocumentData oldDoc = (DocumentData) documents.get(docKey);
                contentSeen.remove(new ContentHashKey(oldDoc.getMd5Hash()));
            }
            String md5 = getMD5(content);
            contentSeen.put(new ContentHashKey(md5), new ContentHashData(url));
            documents.put(docKey, new DocumentData(md5, content, date));
        }
    }

    private class TransactionGetDocument implements TransactionWorker {
        private String url;
        public TransactionGetDocument(String url) { this.url = url; }
        @Override
        public void doWork() throws Exception {
            Map documents = views.getDocumentMap();
            DocumentData doc = (DocumentData) documents.get(new DocumentKey(url));
            throw new GetDocumentResponse(doc);
        }
    }

    private class GetDocumentResponse extends Exception {
        public DocumentData doc;
        public GetDocumentResponse(DocumentData doc) { this.doc = doc; }
    }

    private class CountDocumentResponse extends Exception {
        public int count;
        public CountDocumentResponse(int count) { this.count = count; }
    }



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
}
