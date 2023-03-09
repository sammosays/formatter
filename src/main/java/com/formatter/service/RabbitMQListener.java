package com.formatter.service;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
@Slf4j
public class RabbitMQListener {

    private final AmazonS3 s3 = AmazonS3ClientBuilder
            .standard().withRegion(Regions.US_EAST_1)
            .withCredentials(new EnvironmentVariableCredentialsProvider()) // picks creds from injected secret env vars
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

                // download file from s3
                try {
                    byte[] content = downloadFromS3(key, bucket);
                    log.info("content: " + Arrays.toString(content));

                } catch (Exception e) {
                    System.err.println(String.format("error processing s3 object - key: %s - bucket: %s - %s",
                            key, bucket, e.getMessage()));
                }
            }

        }
    }

    public byte[] downloadFromS3(String key, String bucket) throws IOException {
        S3Object object = s3.getObject(key, bucket);
        return IOUtils.toByteArray(object.getObjectContent());
    }
}