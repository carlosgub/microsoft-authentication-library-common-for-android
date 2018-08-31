// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.microsoft.identity.common.internal.providers.oauth2;

import android.net.Uri;

import com.microsoft.identity.common.Account;
import com.microsoft.identity.common.exception.ClientException;
import com.microsoft.identity.common.internal.net.HttpRequest;
import com.microsoft.identity.common.internal.net.HttpResponse;
import com.microsoft.identity.common.internal.net.ObjectMapper;
import com.microsoft.identity.common.internal.ui.AuthorizationAgent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Future;


/**
 * Serves as the abstract base class for an oAuth2 client implementation; The base class should be extended
 * by Identity Provider specific implementations; For example: Azure Active Directory, ADFS, Microsoft STS, Etc...
 */
public abstract class OAuth2Strategy
        <GenericAccessToken extends AccessToken,
                GenericAccount extends Account,
                GenericAuthorizationRequest extends AuthorizationRequest,
                GenericAuthorizationRequestBuilder extends AuthorizationRequest.Builder,
                GenericAuthorizationStrategy extends AuthorizationStrategy,
                GenericOAuth2Configuration extends OAuth2Configuration,
                GenericAuthorizationResponse extends AuthorizationResponse,
                GenericRefreshToken extends RefreshToken,
                GenericTokenRequest extends TokenRequest,
                GenericTokenResponse extends TokenResponse,
                GenericTokenResult extends TokenResult,
                GenericAuthorizationResult extends AuthorizationResult> {
    protected static final String TOKEN_REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final GenericOAuth2Configuration mConfig;
    private String mTokenEndpoint;
    private Uri mIssuer;

    /**
     * Constructor of OAuth2Strategy.
     *
     * @param config generic OAuth2 configuration
     */
    public OAuth2Strategy(GenericOAuth2Configuration config) {
        mConfig = config;
    }

    /**
     * Template method for executing an OAuth2 authorization request.
     *
     * @param request               generic authorization request.
     * @param authorizationStrategy generic authorization strategy.
     * @return GenericAuthorizationResponse
     */
    public Future<AuthorizationResult> requestAuthorization(
            final GenericAuthorizationRequest request,
            final GenericAuthorizationStrategy authorizationStrategy) {
        validateAuthorizationRequest(request);
        Future<AuthorizationResult> future = null;
        try {
            future = authorizationStrategy.requestAuthorization(request, this);
        } catch (final UnsupportedEncodingException | ClientException exc) {
            //TODO
        }

        return future;
    }

    public abstract AuthorizationResultFactory getAuthorizationResultFactory();

    /**
     * @param request generic token request.
     * @return GenericTokenResult
     * @throws IOException thrown when failed or interrupted I/O operations occur.
     */
    public GenericTokenResult requestToken(final GenericTokenRequest request) throws IOException {
        validateTokenRequest(request);
        HttpResponse response = performTokenRequest(request);
        return getTokenResultFromHttpResponse(response);
    }

    protected HttpResponse performTokenRequest(final GenericTokenRequest request) throws IOException {

        String requestBody = ObjectMapper.serializeObjectToFormUrlEncoded(request);
        Map<String, String> headers = new TreeMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put("client-request-id", correlationId);

        return HttpRequest.sendPost(
                new URL(mTokenEndpoint),
                headers,
                requestBody.getBytes(ObjectMapper.ENCODING_SCHEME),
                TOKEN_REQUEST_CONTENT_TYPE
        );
    }

    protected final void setTokenEndpoint(final String tokenEndpoint) {
        mTokenEndpoint = tokenEndpoint;
    }

    protected GenericOAuth2Configuration getOAuth2Configuration() {
        return mConfig;
    }

    protected Uri getIssuer() {
        return mIssuer;
    }

    protected final void setIssuer(final Uri issuer) {
        mIssuer = issuer;
    }

    /**
     * An abstract method for returning the issuer identifier to be used when caching a token response.
     *
     * @param request generic token request.
     * @return String of issuer cache identifier.
     */
    public abstract String getIssuerCacheIdentifier(GenericAuthorizationRequest request);

    /**
     * @param response generic token response.
     * @return generic access token.
     */
    public abstract GenericAccessToken getAccessTokenFromResponse(GenericTokenResponse response);

    /**
     * @param response generic token response.
     * @return generic refresh token.
     */
    public abstract GenericRefreshToken getRefreshTokenFromResponse(GenericTokenResponse response);

    /**
     * An abstract method for returning the user associated with a request.
     * This could be based on the contents of the ID Token or it could be returned based on making a call
     * to the user_info or profile endpoint associated with a userr: For example: graph.microsoft.com/me
     * This allows IDPs that do not support OIDC to still be able to return a user to us
     * This method should take the TokenResponse as a parameter
     *
     * @param response Generic token response.
     * @return GenericAccount
     */
    public abstract GenericAccount createAccount(GenericTokenResponse response);

    /**
     * Abstract method for creating the authorization request.  In the case of AAD this is the method
     *
     * @return AuthorizationRequest.
     */
    public abstract GenericAuthorizationRequestBuilder createAuthorizationRequestBuilder();

    /**
     * Abstract method for creating the token request.  In the case of AAD this is the method
     *
     * @return TokenRequest.
     */
    public abstract GenericTokenRequest createTokenRequest(GenericAuthorizationRequest request, GenericAuthorizationResponse response);

    /**
     * Abstract method for validating the authorization request.  In the case of AAD this is the method
     * from which the details of the authorization request including authority validation would occur (preferred network and preferred cache)
     *
     * @param request generic authorization request.
     */
    protected abstract void validateAuthorizationRequest(GenericAuthorizationRequest request);

    /**
     * Abstract method for validating the token request.  Generally speaking I expect this just to be validating
     * that all of the information was provided in the Token Request in order to successfully complete it.
     *
     * @param request Generic token request.
     */
    protected abstract void validateTokenRequest(GenericTokenRequest request);

    /**
     * Abstract method for translating the HttpResponse to a TokenResponse.
     *
     * @param response Http response.
     */
    protected abstract GenericTokenResult getTokenResultFromHttpResponse(HttpResponse response);

   // TODO
//    protected abstract void validateAuthorizationResponse(GenericAuthorizationResponse response);

//    protected abstract void validateTokenResponse(GenericTokenResponse response);
}