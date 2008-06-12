package com.nexusbpm.dataselector.model.persistance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.nexusbpm.dataselector.model.LSTree;

public interface TreePersistance {
    /**
     * Return a new tree with the minimum required values set.
     * Required values include: an LSConfig, LSConnection, and LSDriver
     */
    LSTree getNewTree();
    /**
     * Return a tree from the given input stream. The returned tree should
     * at minimum contain an LSConfig, LSConnection, and LSDriver.
     */
    LSTree parseTree(InputStream stream) throws IOException;
    void writeTree(OutputStream stream, LSTree tree) throws IOException;
}
