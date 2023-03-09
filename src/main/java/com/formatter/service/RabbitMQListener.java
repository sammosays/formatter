package com.formatter.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
@Slf4j
public class RabbitMQListener {

    private static final String MINIO_ENDPOINT = "http://minio-service:9000";

    private final AmazonS3 s3 = AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(MINIO_ENDPOINT, Regions.DEFAULT_REGION.getName()))
            .withCredentials(new EnvironmentVariableCredentialsProvider()) // gets creds from injected secret env vars
            .build();

    public void consumeMessage(String message) {
        log.info("Consumed Message: " + message);

        // Process each unpacked file from each record
        JSONObject obj = new JSONObject(message);
        JSONArray records = obj.getJSONArray("Records");
        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            JSONArray unpackedFiles = record.getJSONArray("unpacked");
            for (int q = 0; q < unpackedFiles.length(); q++) {
                JSONObject unpackedFile = unpackedFiles.getJSONObject(q);
                String id = unpackedFile.getString("id");
                String key = unpackedFile.getString("key");
                String bucket = unpackedFile.getString("bucket");
                log.info("found key: {} - bucket: {}", key, bucket);

                try {
                    // download file from s3
                    byte[] content = downloadFromMinio(key, bucket);
                    log.info("content: " + Arrays.toString(content));

                } catch (Exception e) {
                    log.error("error processing s3 object - key: {} - bucket: {} - {}", key, bucket, e.getMessage());
                }
            }

        }
    }

    public byte[] downloadFromMinio(String key, String bucket) throws Exception {
//        S3Object object = s3.getObject(bucket, key);
//        return IOUtils.toByteArray(object.getObjectContent());

        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(MINIO_ENDPOINT)
                        .credentials("AzsPRcioJ8aCuunm", "Mx2VUYufVrYlSQbvijglKUHSQJ1gHFvX")
                        .build();
        InputStream obj = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build());

        return IOUtils.toByteArray(obj);
    }
}