package com.aerotrack.utils.clients.s3;

import com.aerotrack.common.Constants;
import com.aerotrack.model.entities.Airport;
import com.aerotrack.model.entities.AirportsJsonFile;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
public class AerotrackS3Client {
    S3Client s3Client;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Make sure to have Jackson's ObjectMapper.

    public static AerotrackS3Client create() {
        return new AerotrackS3Client(S3Client.create());
    }
    public String getStringObjectFromS3(String objectKey) throws IOException {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(System.getenv(Constants.AIRPORTS_BUCKET_ENV_VAR))
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
                .bucket(System.getenv(Constants.AIRPORTS_BUCKET_ENV_VAR))
                .key(objectKey)
                .build();

        RequestBody requestBody = RequestBody.fromString(object.toString());

        s3Client.putObject(putObjectRequest, requestBody);
    }

    private AirportsJsonFile getAirportsJsonFile(String objectKey) throws IOException {
        String jsonContent = getStringObjectFromS3(objectKey);
        return objectMapper.readValue(jsonContent, AirportsJsonFile.class);
    }

    public AirportsJsonFile getRyanairAirports() throws IOException {
        return getAirportsJsonFile(Constants.RYANAIR_AIRPORTS_OBJECT_NAME);
    }

    public AirportsJsonFile getWizzairAirports() throws IOException {
        return getAirportsJsonFile(Constants.WIZZAIR_AIRPORTS_OBJECT_NAME);
    }

    public AirportsJsonFile getMergedAirports() throws IOException {
        AirportsJsonFile ryanairAirports = getRyanairAirports();
        AirportsJsonFile wizzairAirports = getWizzairAirports();

        Set<Airport> mergedSet = new HashSet<>(ryanairAirports.getAirports());
        for (Airport wizzairAirport : wizzairAirports.getAirports()) {
            if (mergedSet.contains(wizzairAirport)) {
                mergedSet.stream()
                        .filter(airport -> airport.equals(wizzairAirport))
                        .findFirst()
                        .ifPresent(airport -> airport.getConnections().addAll(wizzairAirport.getConnections()));
            } else {
                mergedSet.add(wizzairAirport);
            }
        }
        return new AirportsJsonFile(mergedSet);
    }
}
