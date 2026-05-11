package de.saschadoemer.agrirouter.mcp.persistence;

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

    private static final String STORAGE_FILE = "storage.properties";
    private static final String CUSTOM_STORAGE_FILE = "temp/subdir/custom.properties";

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
    void testSaveAndLoadTenantId() {
        String tenantId = "test-tenant-id";
        persistenceService.saveTenantId(tenantId);

        Optional<String> loadedTenantId = persistenceService.loadTenantId();
        assertTrue(loadedTenantId.isPresent());
        assertEquals(tenantId, loadedTenantId.get());
        assertTrue(Files.exists(Paths.get(STORAGE_FILE)));
    }

    @Test
    void testLoadNonExistentTenantId() {
        Optional<String> loadedTenantId = persistenceService.loadTenantId();
        assertFalse(loadedTenantId.isPresent());
    }

    @Test
    void testCustomStoragePath() throws IOException {
        try (ApplicationContext context = ApplicationContext.run(Map.of("persistence.storage-path", CUSTOM_STORAGE_FILE))) {
            PersistenceService customService = context.getBean(PersistenceService.class);
            String tenantId = "custom-tenant-id";
            customService.saveTenantId(tenantId);

            Optional<String> loadedTenantId = customService.loadTenantId();
            assertTrue(loadedTenantId.isPresent());
            assertEquals(tenantId, loadedTenantId.get());
            assertTrue(Files.exists(Paths.get(CUSTOM_STORAGE_FILE)));
        }
    }
}
