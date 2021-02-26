package com.sphereon.examples.api.blockchainproof.controllers;

import com.sphereon.examples.api.blockchainproof.config.ConfigurationGenerator;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.ConfigurationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ConfigurationResponse;
import org.springframework.stereotype.Controller;

@Controller
public class ConfigurationController {

    private final TokenRequest tokenRequester;
    private final ConfigurationApi configurationApi;
    private final ConfigurationGenerator configurationGenerator;


    public ConfigurationController(final TokenRequest tokenRequester,
                                   final ConfigurationApi configurationApi,
                                   final ConfigurationGenerator configurationGenerator) {
        this.tokenRequester = tokenRequester;
        this.configurationApi = configurationApi;
        this.configurationGenerator = configurationGenerator;
    }


    public void checkConfiguration(final String configName) {
        tokenRequester.execute();
        try {
            getOrCreateConfiguration(configName);
        } catch (ApiException e) {
            throw new RuntimeException(String.format("Configuration request failed with http code %d and message: %s. The response body was %n%s",
                    e.getCode(), e.getMessage(), e.getResponseBody()));
        }
    }


    private ConfigurationResponse getOrCreateConfiguration(final String configName) throws ApiException {
        try {
            final var response = configurationApi.getConfiguration(configName);
            return response;
        } catch (ApiException e) {
            if (e.getCode() == 404) { // Not found
                return configurationApi.createConfiguration(configurationGenerator.generateDefaultConfiguration(configName));
            } else {
                throw e;
            }
        }
    }
}
