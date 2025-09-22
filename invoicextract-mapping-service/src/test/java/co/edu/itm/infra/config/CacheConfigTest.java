package co.edu.itm.infra.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;

class CacheConfigTest {

    @Test
    void cacheManagerProvidesMappingsByErpCache() {
        CacheConfig config = new CacheConfig();
        CacheManager manager = config.cacheManager();
        assertNotNull(manager);
        Cache cache = manager.getCache("mappingsByErp");
        assertNotNull(cache);
        cache.put("key", "value");
        assertEquals("value", cache.get("key", String.class));
    }
}
