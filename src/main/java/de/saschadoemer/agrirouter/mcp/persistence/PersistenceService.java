package de.saschadoemer.agrirouter.mcp.persistence;

import de.saschadoemer.agrirouter.mcp.dto.AgrirouterState;
import io.micronaut.context.annotation.Value;
import io.micronaut.serde.ObjectMapper;
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

    private final ObjectMapper objectMapper;

    public PersistenceService(@Value("${persistence.storage-path:storage.json}") String storagePath, ObjectMapper objectMapper) {
        this.storagePath = Paths.get(storagePath);
        this.objectMapper = objectMapper;
    }

    /**
     * Save the agrirouter state.
     *
     * @param state the agrirouter state
     */
    public void save(AgrirouterState state) {
        try {
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }
            final String json = objectMapper.writeValueAsString(state);
            Files.writeString(storagePath, json);
            LOG.info("Successfully saved state to {}.", storagePath.toAbsolutePath());
        } catch (IOException e) {
            LOG.error("Could not save state to {}.", storagePath.toAbsolutePath(), e);
        }
    }

    /**
     * Load the agrirouter state.
     *
     * @return the agrirouter state
     */
    public Optional<AgrirouterState> load() {
        if (Files.exists(storagePath)) {
            try {
                String json = Files.readString(storagePath);
                LOG.info("Successfully loaded state from {}.", storagePath.toAbsolutePath());
                if (json.isEmpty()) {
                    return Optional.empty();
                }
                return Optional.of(objectMapper.readValue(json, AgrirouterState.class));
            } catch (IOException e) {
                LOG.error("Could not load state from {}.", storagePath.toAbsolutePath(), e);
            }
        }
        return Optional.empty();
    }


}
