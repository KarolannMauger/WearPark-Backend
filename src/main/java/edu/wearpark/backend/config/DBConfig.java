package edu.wearpark.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.MongoDbFactoryParser;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
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
