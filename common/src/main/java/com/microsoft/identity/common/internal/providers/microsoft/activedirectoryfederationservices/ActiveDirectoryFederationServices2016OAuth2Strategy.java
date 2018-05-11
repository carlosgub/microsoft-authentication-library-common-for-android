package com.microsoft.identity.common.internal.providers.microsoft.activedirectoryfederationservices;

import android.net.Uri;

import com.microsoft.identity.common.Account;
import com.microsoft.identity.common.internal.net.HttpResponse;
import com.microsoft.identity.common.internal.providers.oauth2.AccessToken;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationRequest;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationResponse;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationStrategy;
import com.microsoft.identity.common.internal.providers.oauth2.OAuth2Configuration;
import com.microsoft.identity.common.internal.providers.oauth2.OAuth2Strategy;
import com.microsoft.identity.common.internal.providers.oauth2.RefreshToken;
import com.microsoft.identity.common.internal.providers.oauth2.TokenRequest;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResponse;
import com.microsoft.identity.common.internal.providers.oauth2.TokenResult;

/**
 * Azure Active Directory Federation Services 2016 oAuth2 Strategy
 * For information on ADFS 2016 oAuth and OIDC support
 * see <a href='https://docs.microsoft.com/en-us/windows-server/identity/ad-fs/overview/ad-fs-scenarios-for-developers'>https://docs.microsoft.com/en-us/windows-server/identity/ad-fs/overview/ad-fs-scenarios-for-developers</a>
 */
public class ActiveDirectoryFederationServices2016OAuth2Strategy extends OAuth2Strategy {
    public ActiveDirectoryFederationServices2016OAuth2Strategy(OAuth2Configuration config) {
        super(config);
    }

    @Override
    public AuthorizationResponse requestAuthorization(AuthorizationRequest request, AuthorizationStrategy authorizationStrategy) {
        return super.requestAuthorization(request, authorizationStrategy);
    }

    @Override
    protected Uri createAuthorizationUri() {
        return super.createAuthorizationUri();
    }

    @Override
    public String getIssuerCacheIdentifier(AuthorizationRequest request) {
        return null;
    }

    @Override
    public AccessToken getAccessTokenFromResponse(TokenResponse response) {
        return null;
    }

    @Override
    public RefreshToken getRefreshTokenFromResponse(TokenResponse response) {
        return null;
    }

    @Override
    public Account createAccount(TokenResponse response) {
        return null;
    }

    /**
     * GET https://fs.contoso.com/adfs/oauth2/authorize?
     *
     * The authorization request containing
     *         response_type: "code"
     *         resource: RP ID (Identifier) of Web API in application group
     *         client_id: client Id of the native application in the application group
     *         redirect_uri: redirect URI of native application in application group
     *
     * @param request
     */
    @Override
    protected void validateAuthorizationRequest(AuthorizationRequest request) {
    }

    /**
     * AD FS responds by returning an authorization code as the "code" parameter in the query
     * component of the redirect_uri.
     * For example: HTTP/1.1 302 Found Location: http://redirect_uri:80/?code=<code>;
     *
     * @param response
     */
    protected void validateAuthorizationResponse(HttpResponse response) {

    }

    /**
     * POST https://fs.contoso.com/adfs/oauth2/token
     *
     *
     *         grant_type: "authorization_code"
     *         code
     *         resource
     *         client_id
     *         redirect_uri
     * @param request
     */
    @Override
    protected void validateTokenRequest(TokenRequest request) {

    }

    /**
     * After the access token expires, ADAL/MSAL will automatically send a refresh token based request
     * to the AD FS token endpoint (skipping the authorization request automatically).
     * Refresh token request:
     * POST https://fs.contoso.com/adfs/oauth2/token
     *
     * Parameter     |  Value
     * ================================
     * grant_type    |  "refresh_token"
     * resource      |  RP ID (Identifier) of Web API in application group
     * client_id     |  client Id of the native application in the application group
     * refresh_token | the refresh token issued by AD FS in response to the initial token request
     */
    protected void validateRefreshTokenRequest() {

    }

    /**
     * AD FS responds with an HTTP 200 with the access_token, refresh_token, and id_token in the body.
     * @param response
     */
    @Override
    protected void validateTokenResponse(HttpResponse response) {

    }

    @Override
    protected TokenResult getTokenResultFromHttpResponse(HttpResponse response) {
        return null;
    }
}
