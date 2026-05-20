package edu.wearpark.backend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Configuration
@EnableMongoAuditing
public class DBConfig extends AbstractMongoClientConfiguration {
    @Value("${db.uri}")
    protected String DB_URI;
    @Value("${db.name}")
    protected String DB_NAME;

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(DB_URI);
    }

    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabase(
            MongoClient mongoClient
    ) {
        return MongoDatabaseFactory.create(mongoClient, DB_NAME);
    }

    @Override
    protected String getDatabaseName() {
        return DB_NAME;
    }

    @Override
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(
                new DateToInstantConverter(),
                new InstantToDateConverter()
        ));
    }

    @WritingConverter
    static class InstantToDateConverter implements Converter<Instant, Date> {
        @Override
        public Date convert(@NonNull Instant source) {
            return Date.from(source);
        }
    }

    @ReadingConverter
    static class DateToInstantConverter implements Converter<Date, Instant> {
        @Override
        public Instant convert(@NonNull Date source) {
            return source.toInstant();
        }
    }
}