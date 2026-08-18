package com.bookguest;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
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

    @Value("${firebase.credentials.base64:}")
    private String firebaseCredentialsBase64;

    @Value("${firebase.credentials.path:}")
    private String firebaseCredentialsPath;

    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials credenciales;

        try (InputStream entrada = abrirCredenciales()) {
            credenciales = GoogleCredentials.fromStream(entrada);
        }

        return StorageOptions.newBuilder()
                .setCredentials(credenciales)
                .build()
                .getService();
    }

    private InputStream abrirCredenciales() throws IOException {
        if (firebaseCredentialsBase64 != null && !firebaseCredentialsBase64.isBlank()) {
            try {
                byte[] contenido = Base64.getDecoder().decode(firebaseCredentialsBase64.trim());
                return new ByteArrayInputStream(contenido);
            } catch (IllegalArgumentException e) {
                throw new IOException("FIREBASE_CREDENTIALS_BASE64 no contiene Base64 válido.", e);
            }
        }

        if (firebaseCredentialsPath != null && !firebaseCredentialsPath.isBlank()) {
            return Files.newInputStream(Path.of(firebaseCredentialsPath.trim()));
        }

        ClassPathResource recurso = new ClassPathResource(firebaseJsonPath + "/" + firebaseJsonFile);
        return recurso.getInputStream();
    }
}
