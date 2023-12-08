package com.aerotrack.utils;

import org.json.JSONObject;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// This will be soon placed in another dependency
public class S3Utils {
    public static JSONObject getJsonObjectFromS3(S3Client s3Client, String bucketName, String objectKey) throws IOException {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        String stringObject = new String(s3Object.readAllBytes(), StandardCharsets.UTF_8);

        return new JSONObject(stringObject);
    }

    public static void putJsonObjectToS3(S3Client s3Client, String bucketName, String objectKey, JSONObject object) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        RequestBody requestBody = RequestBody.fromString(object.toString());

        s3Client.putObject(putObjectRequest, requestBody);
    }
}
