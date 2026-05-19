package edu.wearpark.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@EntityScan("edu.wearpark.backend")
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
}
