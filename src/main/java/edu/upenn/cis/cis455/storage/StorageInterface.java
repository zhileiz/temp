package edu.upenn.cis.cis455.storage;

import edu.upenn.cis.cis455.model.Channel;

import java.util.List;

public interface StorageInterface {
    
    /**
     * How many documents so far?
     */
	public int getCorpusSize();
	
	/**
	 * Add a new document, getting its ID
	 */
	public int addDocument(String url, String documentContents, String date);
	
	/**
	 * How many keywords so far?
	 */
	public int getLexiconSize();
	
	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	public int addOrGetKeywordId(String keyword);
	
	/**
	 * Adds a user and returns an ID
	 */
	public int addUser(String username, String firstName, String lastName, String password);

	int addChannel(String channelName, String xpath, String creator);

	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	public boolean getSessionForUser(String username, String password);
	
	/**
	 * Retrieves a document's contents by URL
	 */
	public Object getDocument(String url);

	public List<String> getAllDocumentURLs();

    List<Channel> getAllChannels();

    Channel getChannelByName(String channelName);

    void addDocumentToChannel(String channelName, String url);

    /**
	 * Shuts down / flushes / closes the storage system
	 */
	public void close();
}
