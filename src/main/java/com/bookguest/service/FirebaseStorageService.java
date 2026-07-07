package com.bookguest.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FirebaseStorageService {

    private final Storage storage;

    @Value("${firebase.bucket.name}")
    private String bucketName;

    @Value("${firebase.storage.path}")
    private String storagePath;

    public FirebaseStorageService(Storage storage) {
        this.storage = storage;
    }

    public String subirImagen(MultipartFile archivo, String carpeta) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarImagen(archivo);

        String extension = obtenerExtension(archivo.getOriginalFilename());
        String carpetaNormalizada = normalizarCarpeta(carpeta);
        String token = UUID.randomUUID().toString();
        String nombreArchivo = storagePath + "/" + carpetaNormalizada + "/" + UUID.randomUUID() + extension;

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, nombreArchivo))
                .setContentType(archivo.getContentType())
                .setMetadata(Map.of("firebaseStorageDownloadTokens", token))
                .build();

        storage.create(blobInfo, archivo.getBytes());

        String nombreCodificado = URLEncoder.encode(nombreArchivo, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return "https://firebasestorage.googleapis.com/v0/b/"
                + bucketName
                + "/o/"
                + nombreCodificado
                + "?alt=media&token="
                + token;
    }

    public void eliminarImagen(String rutaImagen) {

        if (rutaImagen == null || rutaImagen.isBlank()) {
            return;
        }

        String nombreArchivo = extraerNombreArchivo(rutaImagen);

        if (nombreArchivo != null && !nombreArchivo.isBlank()) {
            storage.delete(BlobId.of(bucketName, nombreArchivo));
        }
    }

    private void validarImagen(MultipartFile archivo) {

        String tipoContenido = archivo.getContentType();

        if (tipoContenido == null || !tipoContenido.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen.");
        }
    }

    private String obtenerExtension(String nombreOriginal) {

        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }

        return nombreOriginal.substring(nombreOriginal.lastIndexOf(".")).toLowerCase();
    }

    private String normalizarCarpeta(String carpeta) {

        if (carpeta == null || carpeta.isBlank()) {
            return "general";
        }

        return carpeta.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9_-]", "");
    }

    private String extraerNombreArchivo(String rutaImagen) {

        try {
            int inicio = rutaImagen.indexOf("/o/");
            int fin = rutaImagen.indexOf("?");

            if (inicio == -1 || fin == -1) {
                return null;
            }

            String nombreCodificado = rutaImagen.substring(inicio + 3, fin);

            return URLDecoder.decode(nombreCodificado, StandardCharsets.UTF_8);

        } catch (Exception e) {
            return null;
        }
    }
}
