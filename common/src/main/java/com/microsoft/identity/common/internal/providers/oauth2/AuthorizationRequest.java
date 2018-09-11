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
import android.util.Base64;

import com.google.gson.annotations.SerializedName;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.net.ObjectMapper;
import com.microsoft.identity.common.internal.util.StringUtil;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

/**
 * A class holding the state of the Authorization Request (OAuth 2.0).
 * https://tools.ietf.org/html/rfc6749#section-4.1.1
 * This should include all fo the required parameters of the authorization request for oAuth2
 * This should provide an extension point for additional parameters to be set
 */
public abstract class AuthorizationRequest<T extends AuthorizationRequest<T>> implements Serializable {
    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 6171895895590170062L;

    /**
     * A required value and must be set to "code".
     */
    @SerializedName("response_type")
    private String mResponseType;

    /**
     * A required value.
     * <p>
     * The client identifier as assigned by the authorization server, when the client was registered.
     */
    @SerializedName("client_id")
    private String mClientId;

    /**
     * Redirect URLs are a critical part of the OAuth flow. After a user successfully authorizes an
     * application, the authorization server will redirect the user back to the application with
     * either an authorization code or access token in the URL.
     */
    @SerializedName("redirect_uri")
    private String mRedirectUri;

    /**
     * A recommended value.
     * <p>
     * A value included in the request that will also be returned in the token response.
     * It can be a string of any content that you wish. A randomly generated unique value is
     * typically used for preventing cross-site request forgery attacks. The value can also
     * encode information about the user's state in the app before the authentication request
     * occurred, such as the page or view they were on.
     */
    @SerializedName("state")
    protected String mState;

    /**
     * Scopes scopes that you want the user to consent to is required for V2 auth request.
     */
    @SerializedName("scope")
    private String mScope;

    /**
     * Constructor of AuthorizationRequest.
     */
    protected AuthorizationRequest(final Builder builder) {
        mResponseType = builder.mResponseType;
        mClientId = builder.mClientId;
        mRedirectUri = builder.mRedirectUri;
        mState = builder.mState;
        mScope = builder.mScope;
    }

    public static final class ResponseType {
        public static final String CODE = "code";
    }

    public static abstract class Builder<B extends AuthorizationRequest.Builder<B>> {
        private String mResponseType = ResponseType.CODE; //ResponseType.CODE as default.
        private String mClientId;
        private String mRedirectUri;
        private String mState;
        private String mScope;

        /**
         * Can be used to pre-fill the username/email address field of the sign-in page for the user, if you know their username ahead of time.
         */
        public String mLoginHint;
        /**
         * Correlation ID.
         */
        public UUID mCorrelationId;
        /**
         * Extra query parameters.
         */
        public String mExtraQueryParam; //TODO not serializable

        public String mPrompt;

        public B setPrompt(String prompt) {
            mPrompt = prompt;
            return self();
        }

        public B setResponseType(String responseType) {
            mResponseType = responseType;
            return self();
        }

        public B setClientId(String clientId) {
            mClientId = clientId;
            return self();
        }

        public B setRedirectUri(String redirectUri) {
            mRedirectUri = redirectUri;
            return self();
        }

        public B setState(String state) {
            mState = state;
            return self();
        }

        public B setScope(String scope) {
            mScope = scope;
            return self();
        }

        public B setLoginHint(String loginHint) {
            mLoginHint = loginHint;
            return self();
        }

        public B setCorrelationId(UUID correlationId) {
            mCorrelationId = correlationId;
            return self();
        }

        public B setExtraQueryParam(String extraQueryParam) {
            mExtraQueryParam = extraQueryParam;
            return self();
        }

        public abstract B self();

        public abstract AuthorizationRequest build();

    }
//
//    /**
//     * Return the start URL to load in the web view.
//     *
//     * @return String of start URL.
//     * @throws UnsupportedEncodingException
//     * @throws ClientException
//     */
//    public abstract String getAuthorizationStartUrl() throws UnsupportedEncodingException, ClientException;

    /**
     * @return Response type of the authorization request.
     */
    public String getResponseType() {
        return mResponseType;
    }

    /**
     * @return mClientId of the authorization request.
     */
    public String getClientId() {
        return mClientId;
    }

    /**
     * @return mRedirectUri of the authorization request.
     */
    public String getRedirectUri() {
        return mRedirectUri;
    }

    /**
     * @return mState of the authorization request.
     */
    public String getState() {
        return mState;
    }

    //CHECKSTYLE:OFF
    @Override
    public String toString() {
        return "AuthorizationRequest{" +
                "mResponseType='" + mResponseType + '\'' +
                ", mClientId='" + mClientId + '\'' +
                ", mRedirectUri='" + mRedirectUri + '\'' +
                ", mScope='" + mScope + '\'' +
                ", mState='" + mState + '\'' +
                '}';
    }

    public String getScope() {
        return mScope;
    }

    public static String generateEncodedState() {
        final UUID stateUUID1 = UUID.randomUUID();
        final UUID stateUUID2 = UUID.randomUUID();
        final String state = stateUUID1.toString() + "-" + stateUUID2.toString();

        String encodedState;

        try {
            encodedState = Base64.encodeToString(state.getBytes("UTF-8"), Base64.NO_PADDING | Base64.URL_SAFE | Base64.NO_WRAP);
        } catch (Exception e) {
            throw new IllegalStateException("Error generating encoded state parameter for Authorization Request", e);
        }

        return encodedState;

    }

    public static String decodeState(final String encodedState) {
        if (StringUtil.isEmpty(encodedState)) {
            Logger.warn("", "Decode state failed because the input state is empty.");
            return null;
        }

        final byte[] stateBytes = Base64.decode(encodedState, Base64.NO_PADDING | Base64.URL_SAFE);
        return new String(stateBytes, Charset.defaultCharset());
    }

    public abstract String getAuthorizationEndpoint();

    public Uri getAuthorizationRequestAsHttpRequest() throws UnsupportedEncodingException {
        Uri.Builder uriBuilder = Uri.parse(getAuthorizationEndpoint()).buildUpon();
        for (Map.Entry<String, String> entry : ObjectMapper.serializeObjectHashMap(this).entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        return uriBuilder.build();
    }
}
