package com.nexturn.vehicleregistration.service;

import org.springframework.core.io.Resource;

/**
 * Where uploaded document files are kept. The local-disk implementation is used today; an S3
 * implementation can replace it without changing the document service.
 */
public interface DocumentStorage {

    /** Saves the bytes under a new, unguessable key inside {@code folder} and returns the key. */
    String store(String folder, String extension, byte[] content);

    Resource load(String key);

    /** Deletes the file if it exists. Never throws for a missing file. */
    void delete(String key);
}
