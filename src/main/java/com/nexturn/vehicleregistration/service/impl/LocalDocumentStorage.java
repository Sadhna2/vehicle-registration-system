package com.nexturn.vehicleregistration.service.impl;

import com.nexturn.vehicleregistration.exception.DocumentStorageException;
import com.nexturn.vehicleregistration.service.DocumentStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Stores documents on local disk under {@code vrs.documents.storage-location} (default ./uploads),
 * outside the web root. File names are random UUIDs; the uploader's file name is never used as a
 * path, and every resolved path is checked to stay inside the storage root.
 */
@Component
public class LocalDocumentStorage implements DocumentStorage {

  private static final Logger LOG = LoggerFactory.getLogger(LocalDocumentStorage.class);

  private final Path root;

  public LocalDocumentStorage(
      @Value("${vrs.documents.storage-location:./uploads}") String location) {
    this.root = Paths.get(location).toAbsolutePath().normalize();
  }

  @Override
  public String store(String folder, String extension, byte[] content) {
    if (!folder.matches("[A-Za-z0-9-]{1,40}") || !extension.matches("[a-z]{2,4}")) {
      throw new DocumentStorageException("Invalid storage location");
    }
    String key = folder + "/" + UUID.randomUUID() + "." + extension;
    Path target = resolve(key);
    try {
      Files.createDirectories(target.getParent());
      Files.write(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
      return key;
    } catch (IOException failure) {
      LOG.error("Could not store document {}", key, failure);
      throw new DocumentStorageException("The document could not be saved. Please try again.");
    }
  }

  @Override
  public Resource load(String key) {
    Path file = resolve(key);
    if (!Files.isRegularFile(file)) {
      throw new DocumentStorageException("The stored document file is missing.");
    }
    return new FileSystemResource(file);
  }

  @Override
  public void delete(String key) {
    try {
      Files.deleteIfExists(resolve(key));
    } catch (IOException | DocumentStorageException failure) {
      LOG.warn("Could not delete stored document {}", key, failure);
    }
  }

  private Path resolve(String key) {
    Path path = root.resolve(key).normalize();
    if (!path.startsWith(root)) {
      throw new DocumentStorageException("Invalid storage location");
    }
    return path;
  }
}
