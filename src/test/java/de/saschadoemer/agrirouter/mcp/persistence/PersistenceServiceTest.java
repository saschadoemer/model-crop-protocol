package de.saschadoemer.agrirouter.mcp.persistence;

import de.saschadoemer.agrirouter.mcp.dto.AgrirouterState;
import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class PersistenceServiceTest {

    @Inject
    PersistenceService persistenceService;

    private static final String STORAGE_FILE = "storage.json";
    private static final String CUSTOM_STORAGE_FILE = "temp/subdir/custom.json";

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(Paths.get(STORAGE_FILE));
        Path customPath = Paths.get(CUSTOM_STORAGE_FILE);
        Files.deleteIfExists(customPath);
        if (customPath.getParent() != null) {
            Files.deleteIfExists(customPath.getParent());
            if (customPath.getParent().getParent() != null) {
                Files.deleteIfExists(customPath.getParent().getParent());
            }
        }
    }

    @Test
    void testSaveAndLoadState() {
        AgrirouterState state = new AgrirouterState();
        state.setTenantId("test-tenant-id");
        state.setExternalId("test-external-id");
        persistenceService.save(state);

        Optional<AgrirouterState> loadedState = persistenceService.load();
        assertTrue(loadedState.isPresent());
        assertEquals(state.getTenantId(), loadedState.get().getTenantId());
        assertEquals(state.getExternalId(), loadedState.get().getExternalId());
        assertTrue(Files.exists(Paths.get(STORAGE_FILE)));
    }

    @Test
    void testLoadNonExistentState() {
        Optional<AgrirouterState> loadedState = persistenceService.load();
        assertFalse(loadedState.isPresent());
    }

    @Test
    void testCustomStoragePath() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("persistence.storage-path", CUSTOM_STORAGE_FILE))) {
            PersistenceService customService = context.getBean(PersistenceService.class);
            AgrirouterState state = new AgrirouterState();
            state.setTenantId("custom-tenant-id");
            customService.save(state);

            Optional<AgrirouterState> loadedState = customService.load();
            assertTrue(loadedState.isPresent());
            assertEquals(state.getTenantId(), loadedState.get().getTenantId());
            assertTrue(Files.exists(Paths.get(CUSTOM_STORAGE_FILE)));
        }
    }

}
