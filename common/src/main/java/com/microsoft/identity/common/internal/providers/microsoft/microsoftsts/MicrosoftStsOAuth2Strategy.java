package com.microsoft.identity.common.internal.providers.microsoft.microsoftsts;

import com.microsoft.identity.common.Account;

import com.microsoft.identity.common.internal.net.HttpResponse;
import com.microsoft.identity.common.internal.net.ObjectMapper;
import com.microsoft.identity.common.internal.providers.microsoft.MicrosoftTokenErrorResponse;
import com.microsoft.identity.common.internal.providers.microsoft.MicrosoftTokenResponse;
import com.microsoft.identity.common.exception.ServiceException;

import com.microsoft.identity.common.internal.providers.microsoft.azureactivedirectory.AzureActiveDirectory;
import com.microsoft.identity.common.internal.providers.microsoft.azureactivedirectory.AzureActiveDirectoryCloud;
import com.microsoft.identity.common.internal.providers.microsoft.azureactivedirectory.ClientInfo;
import com.microsoft.identity.common.internal.providers.oauth2.AccessToken;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationRequest;
import com.microsoft.identity.common.internal.providers.oauth2.IDToken;
import com.microsoft.identity.common.internal.providers.oauth2.OAuth2Strategy;
import com.microsoft.identity.common.internal.providers.oauth2.RefreshToken;
import com.microsoft.identity.common.internal.providers.oauth2.TokenErrorResponse;
import com.microsoft.identity.common.internal.providers.oauth2.TokenRequest;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResponse;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResult;

import java.net.URL;

public class MicrosoftStsOAuth2Strategy extends OAuth2Strategy {

    private MicrosoftStsOAuth2Configuration mConfig;


    public MicrosoftStsOAuth2Strategy(MicrosoftStsOAuth2Configuration config) {
        super(config);
        mConfig = config;
        mTokenEndpoint = "https://login.microsoftonline.com/microsoft.com/oAuth2/v2.0/token";
    }

    @Override
    public String getIssuerCacheIdentifier(AuthorizationRequest request) {
        if (!(request instanceof MicrosoftStsAuthorizationRequest)) {
            throw new IllegalArgumentException("Request provided is not of type MicrosoftStsAuthorizationRequest");
        }

        final URL authority = ((MicrosoftStsAuthorizationRequest) request).getAuthority();
        // TODO I don't think this is right... This is probably not the correct authority cache to consult...
        final AzureActiveDirectoryCloud cloudEnv = AzureActiveDirectory.getAzureActiveDirectoryCloud(authority);
        // This map can only be consulted if authority validation is on.
        // If the host has a hardcoded trust, we can just use the hostname.
        if (null != cloudEnv) {
            return cloudEnv.getPreferredNetworkHostName();
        }
        return authority.getHost();
    }

    @Override
    public AccessToken getAccessTokenFromResponse(TokenResponse response) {
        if (!(response instanceof MicrosoftStsTokenResponse)) {
            throw new IllegalArgumentException("Expected MicrosoftStsTokenResponse in MicrosoftStsOAuth2Strategy.getAccessTokenFromResponse");
        }
        return new MicrosoftStsAccessToken(response);
    }

    @Override
    public RefreshToken getRefreshTokenFromResponse(TokenResponse response) {
        if (!(response instanceof MicrosoftStsTokenResponse)) {
            throw new IllegalArgumentException("Expected AzureActiveDirectoryTokenResponse in AzureActiveDirectoryOAuth2Strategy.getRefreshTokenFromResponse");
        }
        return new MicrosoftStsRefreshToken((MicrosoftStsTokenResponse) response);
    }

    @Override
    public Account createAccount(TokenResponse response) {
        IDToken idToken = null;
        ClientInfo clientInfo = null;
        try {
            idToken = new IDToken(response.getIdToken());
            clientInfo = new ClientInfo(((MicrosoftStsTokenResponse) response).getClientInfo());
        } catch (ServiceException ccse) {
            // TODO: Add a log here
            // TODO: Should we bail?
        }

        return MicrosoftStsAccount.create(idToken, clientInfo);
    }

    @Override
    protected void validateAuthorizationRequest(AuthorizationRequest request) {
        // TODO implement
    }

    @Override
    protected void validateTokenRequest(TokenRequest request) {
        // TODO implement
    }

    /**
     * There are two kinds of token response in the {@link HttpResponse#mResponseBody} in AAD v2.0,
     * successful response and error response.
     *
     * The successful response is JSON string containing
     *             access_token
     *             token_type
     *             expires_in
     *             scope
     *             refresh_token: only provided if `offline_access` scope was requested.
     *             id_token: an unsigned JSON Web Token (JWT). Only provided if `openid` scope was requested.
     *
     * The token issuance endpoint errors are HTTP error codes, because the client calls the token
     * issuance endpoint directly. In addition to the HTTP status code, the Azure AD token issuance
     * endpoint also returns a JSON document with objects that describe the error.
     *             error
     *             error_description in JSON format
     *             error_codes
     *             timestamp
     *             trace_id
     *             correlation_id
     *
     * This function is only used to check if the {@link HttpResponse#mResponseBody} is in the right JSON format.
     * @param response
     */
    @Override
    protected void validateTokenResponse(HttpResponse response) {

    }

    @Override
    protected TokenResult getTokenResultFromHttpResponse(HttpResponse response) {
        //Valid the httpResponse

        TokenResponse tokenResponse = null;
        TokenErrorResponse tokenErrorResponse = null;

        if (response.getStatusCode() >= 400) {
            //An error occurred
            tokenErrorResponse = ObjectMapper.deserializeJsonStringToObject(response.getBody(), MicrosoftTokenErrorResponse.class);
        } else {
            tokenResponse = ObjectMapper.deserializeJsonStringToObject(response.getBody(), MicrosoftTokenResponse.class);
        }

        return new TokenResult(tokenResponse, tokenErrorResponse);

    }
}
