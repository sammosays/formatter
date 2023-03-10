package com.formatter.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


@Service
@Slf4j
public class Formatter {

    private static final String FORMATTED_BUCKET = "formatted";
    private static final String MINIO_ENDPOINT = "http://minio-service:9000";
    private static final String MINIO_SECRET_ENV_VAR = "MINIO_ACCESS_SECRET";
    private static final String MINIO_TOKEN_ENV_VAR = "MINIO_ACCESS_TOKEN";
    private static final String PUBLISH_EXCHANGE = "dw";
    private static final String PUBLISH_ROUTING_KEY = "processed-queue";

    private MinioClient minioClient = null;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public void consumeMessageFromRabbit(String message) {
        log.info("Consumed Message: " + message);
        JSONArray updatedRecords = new JSONArray();

        // Process each unpacked file from each record
        JSONObject msg = new JSONObject(message);
        JSONArray records = msg.getJSONArray("Records");
        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            JSONArray unpackedFiles = record.getJSONArray("unpacked");

            JSONArray formattedObjects = new JSONArray();
            for (int q = 0; q < unpackedFiles.length(); q++) {

                JSONObject unpackedFile = unpackedFiles.getJSONObject(q);
                String id = unpackedFile.getString("id");
                String key = unpackedFile.getString("key");
                String bucket = unpackedFile.getString("bucket");
                log.info("found key: {} - bucket: {}", key, bucket);

                try {
                    // download file from MinIO
                    InputStream content = downloadFromMinio(key, bucket);
                    log.info("downloaded - key: {} - bucket: {}", key, bucket);

                    // format the contents
                    StringBuilder builder = new StringBuilder();
                    String line;

                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content))) {
                        while ((line = bufferedReader.readLine()) != null) {
                            // add newline after each full stop
                            builder.append(line.replaceAll("\\.\\s?", "\\.\n").trim());
                        }
                        log.info("formatted - key: {} - bucket: {}", key, bucket);

                        // upload formatted content to Minio
                        byte[] formattedContent = builder.toString().getBytes();
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(formattedContent)) {
                            uploadToMinio(key, FORMATTED_BUCKET, bais, formattedContent.length);
                            log.info("uploaded formatted content to key: {} - bucket: {}", key, FORMATTED_BUCKET);
                        }

                        // update the message
                        formattedObjects.put(new JSONObject()
                                .put("id", id)
                                .put("key", key)
                                .put("bucket", FORMATTED_BUCKET));
                    }

                } catch (Exception e) {
                    log.error("error - key: {} - bucket: {} - {}", key, bucket, e.getMessage());
                }
            }
            record.append("formatted", formattedObjects);
            updatedRecords.put(record);
        }
        // publish to rabbit
        msg.put("Records", updatedRecords);
        String updatedMessage = msg.toString();
        rabbitTemplate.convertAndSend(PUBLISH_EXCHANGE, PUBLISH_ROUTING_KEY, updatedMessage);
        log.info("published messaged: {}", updatedMessage);
    }

    public MinioClient getMinioClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(MINIO_ENDPOINT)
                    .credentials(System.getenv(MINIO_TOKEN_ENV_VAR), System.getenv(MINIO_SECRET_ENV_VAR))
                    .build();
        }
        return minioClient;
    }

    public InputStream downloadFromMinio(String key, String bucket) throws Exception {
        return getMinioClient().getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build());
    }

    public void uploadToMinio(String key, String bucket, InputStream inputStream, int size) throws Exception {
        getMinioClient().putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .stream(inputStream, size, -1)
                        .build());

    }
}