package co.edu.itm.infra.config;

import co.edu.itm.domain.service.DynamicMappingService;
import co.edu.itm.domain.service.TransformRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {
    @Bean
    public TransformRegistry transformRegistry() {
        return new TransformRegistry();
    }

    @Bean
    public DynamicMappingService dynamicMappingService(TransformRegistry r) {
        return new DynamicMappingService(r);
    }
}
