package edu.upenn.cis.cis455.storage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.collections.StoredEntrySet;
import com.sleepycat.collections.StoredSortedMap;
import edu.upenn.cis.cis455.model.*;

public class BerkeleyViews {

    private StoredSortedMap userMap;
    private StoredSortedMap documentMap;
    private StoredSortedMap contentSeenMap;

    public BerkeleyViews(BerkeleyDatabase db)
    {
        ClassCatalog catalog = db.getClassCatalog();
        EntryBinding userKeyBinding = new SerialBinding(catalog, UserKey.class);
        EntryBinding userDataBinding = new SerialBinding(catalog, UserData.class);
        EntryBinding documentKeyBinding = new SerialBinding(catalog, DocumentKey.class);
        EntryBinding documentDataBinding = new SerialBinding(catalog, DocumentData.class);
        EntryBinding contentSeenKeyBinding = new SerialBinding(catalog, ContentHashKey.class);
        EntryBinding contentSeenDataBinding = new SerialBinding(catalog, ContentHashData.class);

        userMap = new StoredSortedMap(db.getUserDatabase(), userKeyBinding, userDataBinding, true);
        documentMap = new StoredSortedMap(db.getDocumentDatabase(), documentKeyBinding, documentDataBinding, true);
        contentSeenMap = new StoredSortedMap(db.getContentSeenDb(), contentSeenKeyBinding, contentSeenDataBinding, true);
    }

    public final StoredSortedMap getUserMap() {
        return userMap;
    }

    public final StoredSortedMap getDocumentMap() {
        return documentMap;
    }

    public StoredSortedMap getContentSeenMap() {
        return contentSeenMap;
    }

    public final StoredEntrySet getUserEntrySet() {
        return (StoredEntrySet) userMap.entrySet();
    }

    public final StoredEntrySet getDocumentEntrySet() {
        return (StoredEntrySet) documentMap.entrySet();
    }
}
