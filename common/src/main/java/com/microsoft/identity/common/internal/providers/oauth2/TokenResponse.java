package com.microsoft.identity.common.internal.providers.oauth2;

import com.google.gson.annotations.SerializedName;

/**
 * This is the class encapsulating the details of the TokenResponse (oAuth2/OIDC)
 * https://tools.ietf.org/html/rfc6749#section-4.1.4
 * It should include all of the required and optional parameters based on the protocol and
 * support an extension to allow the authorization server / openid provider to send back additional information
 */
public class TokenResponse {

    /**
     * RECOMMENDED.  The lifetime in seconds of the access token.  For
     * example, the value "3600" denotes that the access token will
     * expire in one hour from the time the response was generated.
     * If omitted, the authorization server SHOULD provide the
     * expiration time via other means or document the default value.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749 - Successful Response</a>
     */
    @SerializedName("expires_in")
    protected Long mExpiresIn;

    /**
     * REQUIRED.  The access token issued by the authorization server.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749 - Successful Response</a>
     */
    @SerializedName("access_token")
    protected String mAccessToken;

    /**
     * REQUIRED.  The type of the token issued as described in
     * <a href="https://tools.ietf.org/html/rfc6749#section-7.1">Section 7.1.</a>
     * Value is case insensitive.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749 - Successful Response</a>
     */
    @SerializedName("token_type")
    protected String mTokenType;

    /**
     * OPTIONAL.  The refresh token, which can be used to obtain new
     * access tokens using the same authorization grant as described
     * in <a href="https://tools.ietf.org/html/rfc6749#section-6">Section 6.</a>
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749 - Successful Response</a>
     */
    @SerializedName("refresh_token")
    protected String mRefreshToken;

    /**
     * OPTIONAL, if identical to the scope requested by the client;
     * otherwise, REQUIRED.  The scope of the access token as
     * described by <a href="https://tools.ietf.org/html/rfc6749#section-3.3">Section 3.3.</a>
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-5.1">RFC 6749 - Successful Response</a>
     */
    @SerializedName("scope")
    protected String mScope;

    /**
     * REQUIRED if the "state" parameter was present in the client
     * authorization request.  The exact value received from the
     * client.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.2.2">RFC 6749 - Access Token Response</a>
     */
    protected String mState;

    /**
     * An unsigned JSON Web Token (JWT). The app can base64Url decode the segments of this token
     * to request information about the user who signed in. The app can cache the values and
     * display them, but it should not rely on them for any authorization or security boundaries.
     *
     * @See <a href="https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-protocols-oauth-code">Authorize access to web applications using OAuth 2.0 and Azure Active Directory</a>
     */
    @SerializedName("id_token")
    protected String mIdToken;


    /**
     * A long representing the time at which the response was received in milliseconds since the Unix Epoch
     */
    protected long mResponseReceivedTime;

    /**
     * Gets the response expires_in.
     *
     * @return The expires_in to get.
     */
    public Long getExpiresIn() {
        return mExpiresIn;
    }

    /**
     * Sets the response expires_in.
     *
     * @param expiresIn The expires_in to set.
     */
    public void setExpiresIn(Long expiresIn) {
        this.mExpiresIn = expiresIn;
    }

    /**
     * Gets the response access_token.
     *
     * @return The access_token to get.
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * Sets the response access_token.
     *
     * @param accessToken The access_token to set.
     */
    public void setAccessToken(String accessToken) {
        this.mAccessToken = accessToken;
    }

    /**
     * Gets the response token_type.
     *
     * @return The token_type to get.
     */
    public String getTokenType() {
        return mTokenType;
    }

    /**
     * Sets the response token_type.
     *
     * @param tokenType The token_type to set.
     */
    public void setTokenType(String tokenType) {
        this.mTokenType = tokenType;
    }

    /**
     * Gets the response refresh_token.
     *
     * @return The refresh_token to get.
     */
    public String getRefreshToken() {
        return mRefreshToken;
    }

    /**
     * Sets the response refresh_token.
     *
     * @param refreshToken The refresh_token to set.
     */
    public void setRefreshToken(String refreshToken) {
        this.mRefreshToken = refreshToken;
    }

    /**
     * Gets the response scope.
     *
     * @return The scope to get.
     */
    public String getScope() {
        return mScope;
    }

    /**
     * Sets the response scope.
     *
     * @param scope The scope to set.
     */
    public void setScope(String scope) {
        this.mScope = scope;
    }

    /**
     * Gets the response state.
     *
     * @return The state to get.
     */
    public String getState() {
        return mState;
    }

    /**
     * Sets the response state.
     *
     * @param state The state to set.
     */
    public void setState(String state) {
        this.mState = state;
    }

    /**
     * Gets the response id_token.
     *
     * @return The id_token to get.
     */
    public String getIdToken() {
        return mIdToken;
    }

    /**
     * Sets the response id_token.
     *
     * @param idToken The id_token to set.
     */
    public void setIdToken(String idToken) {
        this.mIdToken = idToken;
    }

    /**
     * Sets the time at which the response was received.  Expressed as milliseconds from the unix epoch
     *
     * @param responseReceivedTime
     */
    public void setResponseReceivedTime(Long responseReceivedTime) {
        this.mResponseReceivedTime = responseReceivedTime;
    }

    /**
     * Gets the time at which the response was received.  Expressed as milliseconds from the unix epoch
     *
     * @return
     */
    public long getResponseReceivedTime() {
        return this.mResponseReceivedTime;
    }

}