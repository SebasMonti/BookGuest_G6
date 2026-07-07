package com.bookguest;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class StorageConfig {

    @Value("${firebase.json.path}")
    private String firebaseJsonPath;

    @Value("${firebase.json.file}")
    private String firebaseJsonFile;

    @Bean
    public Storage storage() throws IOException {
        ClassPathResource recurso = new ClassPathResource(firebaseJsonPath + "/" + firebaseJsonFile);

        GoogleCredentials credenciales = GoogleCredentials.fromStream(recurso.getInputStream());

        return StorageOptions.newBuilder()
                .setCredentials(credenciales)
                .build()
                .getService();
    }
}
