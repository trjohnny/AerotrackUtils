package com.aerotrack.utils.clients;

import com.aerotrack.utils.clients.s3.AerotrackS3Client;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AerotrackS3ClientTest {
    @Mock
    private S3Client mockS3Client;
    @Mock
    private ResponseInputStream<GetObjectResponse> mockResponseStream;
    private AerotrackS3Client aerotrackS3Client;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aerotrackS3Client = new AerotrackS3Client(mockS3Client);
    }

    @Test
    void getStringObjectFromS3_Success() throws Exception {
        String mockS3Response = "Test S3 String Content";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(mockS3Response.getBytes(StandardCharsets.UTF_8));
        when(mockResponseStream.readAllBytes()).thenReturn(mockS3Response.getBytes(StandardCharsets.UTF_8));
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseStream);

        String result = aerotrackS3Client.getStringObjectFromS3("test-key");

        assertEquals("Test S3 String Content", result);
    }

    @Test
    void getJsonObjectFromS3_Success() throws Exception {
        String json = "{\"key\": \"value\"}";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        when(mockResponseStream.readAllBytes()).thenReturn(json.getBytes(StandardCharsets.UTF_8));
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockResponseStream);

        JSONObject result = aerotrackS3Client.getJsonObjectFromS3("test-key");

        assertEquals("value", result.getString("key"));
    }

    @Test
    void putJsonObjectToS3_Success() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key", "value");
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().build());

        aerotrackS3Client.putJsonObjectToS3( "object-key", jsonObject);

        verify(mockS3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
