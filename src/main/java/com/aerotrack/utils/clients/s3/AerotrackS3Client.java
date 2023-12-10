package com.aerotrack.utils.clients.s3;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.aerotrack.utils.Constants.AIRPORTS_BUCKET_ENV_VAR;

@AllArgsConstructor
public class AerotrackS3Client {
    S3Client s3Client;
    public static AerotrackS3Client create() {
        return new AerotrackS3Client(S3Client.create());
    }
    public String getStringObjectFromS3(String objectKey) throws IOException {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(System.getenv(AIRPORTS_BUCKET_ENV_VAR))
                .key(objectKey)
                .build();

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

        return new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);
    }

    public JSONObject getJsonObjectFromS3(String objectKey) throws IOException {
        return new JSONObject(getStringObjectFromS3(objectKey));
    }

    public void putJsonObjectToS3(String objectKey, JSONObject object) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(System.getenv(AIRPORTS_BUCKET_ENV_VAR))
                .key(objectKey)
                .build();

        RequestBody requestBody = RequestBody.fromString(object.toString());

        s3Client.putObject(putObjectRequest, requestBody);
    }
}
