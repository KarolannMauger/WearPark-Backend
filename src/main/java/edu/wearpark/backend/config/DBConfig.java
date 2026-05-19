package edu.wearpark.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.Collections;

@Configuration
@EnableMongoAuditing
public class DBConfig {
    @Value("${db.uri}")
    protected String DB_URI;
    @Value("${db.name}")
    protected String DB_NAME;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(DB_URI);
    }
    @Bean
    public MongoDatabaseFactory mongoDatabase(
            MongoClient mongoClient
    ) {
        return MongoDatabaseFactory.create(mongoClient, DB_NAME);
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory factory,
            MongoMappingContext context
    ) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
        converter.setCustomConversions(new MongoCustomConversions(Collections.emptyList()));
        return converter;
    }
}
