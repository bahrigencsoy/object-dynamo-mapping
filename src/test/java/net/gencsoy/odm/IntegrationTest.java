package net.gencsoy.odm;

import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class IntegrationTest {

    private DynamoDbClient client;

    void step_001_create_client() {
        client = DynamoDbClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.US_EAST_1)
                .build();
    }

    public static void main(String[] args) {
        IntegrationTest test = new IntegrationTest();
        test.step_001_create_client();
    }
}
