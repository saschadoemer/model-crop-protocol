package de.saschadoemer.agrirouter.mcp.persistence;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Lightweight persistence service to store and load data.
 */
@Singleton
public class PersistenceService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class);

    private final Path storagePath;

    public PersistenceService(@Value("${persistence.storage-path:storage.properties}") String storagePath) {
        this.storagePath = Paths.get(storagePath);
    }

    /**
     * Save the tenant ID.
     *
     * @param tenantId the tenant ID
     */
    public void saveTenantId(String tenantId) {
        try {
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }
            Files.writeString(storagePath, tenantId);
            LOG.info("Successfully saved tenant ID to {}.", storagePath.toAbsolutePath());
        } catch (IOException e) {
            LOG.error("Could not save tenant ID to {}.", storagePath.toAbsolutePath(), e);
        }
    }

    /**
     * Load the tenant ID.
     *
     * @return the tenant ID
     */
    public Optional<String> loadTenantId() {
        if (Files.exists(storagePath)) {
            try {
                String tenantId = Files.readString(storagePath).trim();
                LOG.info("Successfully loaded tenant ID from {}.", storagePath.toAbsolutePath());
                return Optional.of(tenantId);
            } catch (IOException e) {
                LOG.error("Could not load tenant ID from {}.", storagePath.toAbsolutePath(), e);
            }
        }
        return Optional.empty();
    }

}
