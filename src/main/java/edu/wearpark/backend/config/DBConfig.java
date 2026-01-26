package edu.wearpark.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig {
    @Bean
    public MongoClient mongoClient(
            @Value("${db.uri}") String uri
    ) {
        return MongoClients.create(uri);
    }
}
