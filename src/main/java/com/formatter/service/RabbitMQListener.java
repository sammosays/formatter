package com.formatter.service;

import com.amazonaws.util.IOUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
@Slf4j
public class RabbitMQListener {

    private static final String MINIO_ENDPOINT = "http://minio-service:9000";
    private static final String MINIO_TOKEN_ENV_VAR = "MINIO_ACCESS_TOKEN";
    private static final String MINIO_SECRET_ENV_VAR = "MINIO_ACCESS_SECRET";

    private final MinioClient minioClient =
            MinioClient.builder()
                    .endpoint(MINIO_ENDPOINT)
                    .credentials(System.getenv(MINIO_TOKEN_ENV_VAR), System.getenv(MINIO_SECRET_ENV_VAR))
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
                    // download file from MinIO
                    byte[] content = downloadFromMinio(key, bucket);
                    log.info("content: " + new String(content));

                } catch (Exception e) {
                    log.error("error processing s3 object - key: {} - bucket: {} - {}", key, bucket, e.getMessage());
                }
            }

        }
    }

    public byte[] downloadFromMinio(String key, String bucket) throws Exception {
        InputStream obj = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(key)
                .build());

        return IOUtils.toByteArray(obj);
    }
}