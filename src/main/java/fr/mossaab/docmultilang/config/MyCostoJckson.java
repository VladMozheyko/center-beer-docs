package fr.mossaab.docmultilang.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class MyCostoJckson {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            StreamReadConstraints constraints = StreamReadConstraints.builder()
                    .maxStringLength(1000 * 1024 * 1024) // 60 МБ
                    .build();
            JsonFactory factory = JsonFactory.builder()
                    .streamReadConstraints(constraints)
                    .build();
            builder.factory(factory);
        };
    }
}
