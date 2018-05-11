package com.microsoft.identity.common.internal.providers.microsoft.azureactivedirectory;

import android.net.Uri;

import com.microsoft.identity.common.Account;

import com.microsoft.identity.common.internal.net.HttpResponse;
import com.microsoft.identity.common.internal.net.ObjectMapper;
import com.microsoft.identity.common.internal.providers.microsoft.MicrosoftTokenErrorResponse;
import com.microsoft.identity.common.exception.ServiceException;

import com.microsoft.identity.common.internal.providers.oauth2.AccessToken;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationRequest;
import com.microsoft.identity.common.internal.providers.oauth2.IDToken;
import com.microsoft.identity.common.internal.providers.oauth2.OAuth2Strategy;
import com.microsoft.identity.common.internal.providers.oauth2.RefreshToken;
import com.microsoft.identity.common.internal.providers.oauth2.TokenErrorResponse;
import com.microsoft.identity.common.internal.providers.oauth2.TokenRequest;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResponse;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResult;

/**
 * The Azure Active Directory oAuth2 Strategy
 */
public class AzureActiveDirectoryOAuth2Strategy extends OAuth2Strategy {

    private AzureActiveDirectoryOAuth2Configuration mConfig = null;

    public AzureActiveDirectoryOAuth2Strategy(AzureActiveDirectoryOAuth2Configuration config) {
        super(config);
        mTokenEndpoint = "https://login.microsoftonline.com/microsoft.com/oauth2/token";
        mConfig = config;
    }

    @Override
    protected void validateAuthorizationRequest(AuthorizationRequest request) {

    }

    /**
     * validate the contents of the token request... all the base class is currently abstract
     * some of the validation for requried parameters for the protocol could be there...
     *
     * @param request
     */
    @Override
    protected void validateTokenRequest(TokenRequest request) {

    }

    /**
     * There are two kinds of token response in the {@link HttpResponse#mResponseBody} in AAD v1.0, successful response and error response.
     * The successful response is JSON string containing
     *             access_token, which is a signed JSON Web Token (JWT)
     *             token_type
     *             expires_in
     *             expires_on
     *             resource
     *             scope
     *             refresh_token
     *             id_token, which is a unsigned JSON Web Token (JWT)
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
     * @param response
     */
    @Override
    protected void validateTokenResponse(HttpResponse response) {

    }

    /**
     * Stubbed out for now, but should create a new AzureActiveDirectory account
     * Should accept a parameter (TokenResponse) for producing that user
     *
     * @return
     */
    @Override
    public Account createAccount(TokenResponse response) {
        IDToken idToken = null;
        ClientInfo clientInfo = null;
        try {
            idToken = new IDToken(response.getIdToken());
            clientInfo = new ClientInfo(((AzureActiveDirectoryTokenResponse) response).getClientInfo());
        } catch (ServiceException ccse) {
            // TODO: Add a log here
            // TODO: Should we bail?
        }
        return AzureActiveDirectoryAccount.create(idToken, clientInfo);
    }

    @Override
    public String getIssuerCacheIdentifier(AuthorizationRequest request) {
        if (!(request instanceof AzureActiveDirectoryAuthorizationRequest)) {
            throw new IllegalArgumentException("Request provided is not of type AzureActiveDirectoryAuthorizationRequest");
        }

        AzureActiveDirectoryAuthorizationRequest authRequest;
        authRequest = (AzureActiveDirectoryAuthorizationRequest) request;
        AzureActiveDirectoryCloud cloud = AzureActiveDirectory.getAzureActiveDirectoryCloud(authRequest.getAuthority());

        if (!cloud.isValidated() && this.mConfig.isAuthorityHostValdiationEnabled()) {
            //We have invalid cloud data... and authority host validation is enabled....
            //TODO: Throw an exception in this case... need to see what ADAL does in this case.
        }

        if (!cloud.isValidated() && !this.mConfig.isAuthorityHostValdiationEnabled()) {
            //Authority host validation not specified... but there is no cloud....
            //Hence just return the passed in Authority
            return authRequest.getAuthority().toString();
        }

        Uri authorityUri = Uri.parse(authRequest.getAuthority().toString())
                .buildUpon()
                .authority(cloud.getPreferredCacheHostName())
                .build();

        return authorityUri.toString();

    }

    @Override
    public AccessToken getAccessTokenFromResponse(TokenResponse response) {
        if (!(response instanceof AzureActiveDirectoryTokenResponse)) {
            throw new IllegalArgumentException(
                    "Expected AzureActiveDirectoryTokenResponse in AzureActiveDirectoryOAuth2Strategy.getAccessTokenFromResponse");
        }
        return new AzureActiveDirectoryAccessToken(response);
    }

    @Override
    public RefreshToken getRefreshTokenFromResponse(TokenResponse response) {
        if (!(response instanceof AzureActiveDirectoryTokenResponse)) {
            throw new IllegalArgumentException(
                    "Expected AzureActiveDirectoryTokenResponse in AzureActiveDirectoryOAuth2Strategy.getRefreshTokenFromResponse");
        }
        return new AzureActiveDirectoryRefreshToken((AzureActiveDirectoryTokenResponse) response);
    }

    @Override
    protected TokenResult getTokenResultFromHttpResponse(HttpResponse response) {
        TokenResponse tokenResponse = null;
        TokenErrorResponse tokenErrorResponse = null;

        if (response.getStatusCode() >= 400) {
            //An error occurred
            tokenErrorResponse = ObjectMapper.deserializeJsonStringToObject(response.getBody(), MicrosoftTokenErrorResponse.class);
        } else {
            tokenResponse = ObjectMapper.deserializeJsonStringToObject(response.getBody(), AzureActiveDirectoryTokenResponse.class);
        }

        return new TokenResult(tokenResponse, tokenErrorResponse);
    }

}
