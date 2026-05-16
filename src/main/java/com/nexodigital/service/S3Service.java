package com.nexodigital.service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

public class S3Service {
    private static S3Client s3Client;
    private static S3Presigner presigner;
    private static String bucketName;
    private static String endpoint;

    static {
        initializeS3Client();
    }

    private static void initializeS3Client() {
        String accessKey = System.getenv("ACCESS_KEY_ID");
        String secretKey = System.getenv("SECRET_ACCESS_KEY");
        String region = System.getenv("REGION");
        endpoint = System.getenv("ENDPOINT");
        bucketName = System.getenv("BUCKET");

        if (accessKey == null || secretKey == null || region == null || endpoint == null || bucketName == null) {
            throw new RuntimeException("S3 environment variables not configured: ACCESS_KEY_ID, SECRET_ACCESS_KEY, REGION, ENDPOINT, BUCKET");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    public static void uploadImage(String fileName, InputStream fileContent, long contentLength) throws Exception {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileContent, contentLength));
        } catch (Exception e) {
            throw new Exception("Error uploading image to S3: " + e.getMessage(), e);
        }
    }

    public static String getImageUrl(String fileName) {
        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(24))
                    .getObjectRequest(b -> b.bucket(bucketName).key(fileName))
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            // Fallback a URL directa si falla la generación de presigned URL
            return endpoint + "/" + bucketName + "/" + fileName;
        }
    }

    public static void closeClient() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (presigner != null) {
            presigner.close();
        }
    }
}
