package edu.wearpark.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.Instant;
import java.util.Date;
import java.util.List;

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
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new DateToInstantConverter(),
                new InstantToDateConverter()
        ));
    }

    @WritingConverter
    static class InstantToDateConverter implements Converter<Instant, Date> {
        public Date convert(Instant source) {
            return Date.from(source);
        }
    }

    @ReadingConverter
    static class DateToInstantConverter implements Converter<Date, Instant> {
        public Instant convert(Date source) {
            return source.toInstant();
        }
    }
}