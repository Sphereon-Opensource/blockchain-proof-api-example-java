package com.sphereon.examples.api.blockchainproof.controllers;

import com.sphereon.examples.api.blockchainproof.enums.UploadMethod;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.sdk.blockchain.proof.api.VerificationApi;
import com.sphereon.sdk.blockchain.proof.handler.ApiException;
import com.sphereon.sdk.blockchain.proof.model.ContentRequest;
import com.sphereon.sdk.blockchain.proof.model.VerifyContentResponse;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;

@Controller
public class VerificationController {

    private final TokenRequest tokenRequester;
    private final VerificationApi verificationApi;
    private final HashingController hashingController;

    @Value("${sphereon.blockchain-proof-api.upload-method}")
    private UploadMethod uploadMethod;


    public VerificationController(final TokenRequest tokenRequester,
                                  final VerificationApi verificationApi,
                                  final HashingController hashingController) {
        this.tokenRequester = tokenRequester;
        this.verificationApi = verificationApi;
        this.hashingController = hashingController;
    }


    public VerifyContentResponse verifyFile(final String configName,
                                            final File targetFile) {
        tokenRequester.execute();

        try {
            switch (uploadMethod) {
                case STREAM:
                    return verifyUsingStream(configName, targetFile);
                case CONTENT:
                    return verifyUsingContent(configName, targetFile);
            }
        } catch (ApiException e) {
            throw new RuntimeException(String.format("Verification request failed with http code %d and message: %s. The response body was %n%s",
                    e.getCode(), e.getMessage(), e.getResponseBody()));
        }
        throw new NotImplementedException("uploadMethod " + uploadMethod);
    }


    private VerifyContentResponse verifyUsingStream(final String configName,
                                                    final File targetFile) throws ApiException {
        return verificationApi.verifyUsingStream(configName, targetFile, null, null, null,
                null, null);
    }


    private VerifyContentResponse verifyUsingContent(final String configName,
                                                     final File targetFile) throws ApiException {
        return verificationApi.verifyUsingContent(configName, buildExistence(targetFile), null, null,
                null, null);
    }


    private ContentRequest buildExistence(final File targetFile) {
        try {
            return new ContentRequest()
                    .hashProvider(ContentRequest.HashProviderEnum.CLIENT) // Using this method you don't actually send your content to the Sphereon cloud
                    .content(hashingController.hashFileToByteArray(targetFile));
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while hashing file " + targetFile.getAbsolutePath());
        }
    }


}
