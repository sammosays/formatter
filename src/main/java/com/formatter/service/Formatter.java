package com.formatter.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Service
@Slf4j
public class Formatter {

    private static final String MINIO_ENDPOINT = "http://minio-service:9000";
    private static final String MINIO_TOKEN_ENV_VAR = "MINIO_ACCESS_TOKEN";
    private static final String MINIO_SECRET_ENV_VAR = "MINIO_ACCESS_SECRET";


    public void consumeMessageFromRabbit(String message) {
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
                    // download file from MinIO
                    InputStream content = downloadFromMinio(key, bucket);
                    //log.info("content: " + new String(content));

                    // format the contents
                    StringBuilder builder = new StringBuilder();
                    String line;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
                    while((line = bufferedReader.readLine()) != null) {
                        // add newline after each full stop
                        builder.append(line.replaceAll("\\.\\s?","\\.\n"));
                    }
                    log.info(builder.toString());

                } catch (Exception e) {
                    log.error("error processing s3 object - key: {} - bucket: {} - {}", key, bucket, e.getMessage());
                }
            }

        }
    }

    public InputStream downloadFromMinio(String key, String bucket) throws Exception {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(MINIO_ENDPOINT)
                        .credentials(System.getenv(MINIO_TOKEN_ENV_VAR), System.getenv(MINIO_SECRET_ENV_VAR))
                        .build();

        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build());

        //return IOUtils.toByteArray(obj);
    }
}