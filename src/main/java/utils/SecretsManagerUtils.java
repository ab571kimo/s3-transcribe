package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.TranscribeParam;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretsManagerUtils {

    public <T> T getSecret(String secretName, Region region, Class<T> clazz) {

        // Create a Secrets Manager client
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        getSecretValueResponse.getValueForField("showSpeakerLabels",Boolean.class);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.fromJson(getSecretValueResponse.secretString(), clazz);

    }
}
