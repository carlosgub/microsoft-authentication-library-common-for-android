package com.microsoft.identity.common.internal.providers.oauth2;

import android.content.Intent;
import android.net.Uri;

import com.microsoft.identity.common.adal.internal.AuthenticationConstants;
import com.microsoft.identity.common.adal.internal.util.HashMapExtensions;
import com.microsoft.identity.common.adal.internal.util.StringExtensions;
import com.microsoft.identity.common.internal.util.StringUtil;

import java.util.Map;

/**
 * A class to return the result of the authorization request to the calling code (ADAL or MSAL Controller classes)
 * This class should have a generic status in terms of : Cancelled, TimedOut, Error,  etc...
 * this class should also contain the AuthorizationResponse which contains the details returned from the
 * In the case of an error/exception this class should return the associated exception
 */
public class AuthorizationResult {
    private static final String TAG = AuthorizationResult.class.getSimpleName();

    private AuthorizationStatus mAuthorizationStatus;

    private AuthorizationResponse mAuthorizationResponse;
    private AuthorizationErrorResponse mAuthorizationError;


    public AuthorizationResponse getAuthorizationResponse() {
        return mAuthorizationResponse;
    }

    public AuthorizationErrorResponse getAuthorizationErrorResponse() {
        return mAuthorizationError;
    }

    private AuthorizationResult(final String authCode, final String state) {
        if (StringUtil.isEmpty(authCode) || StringUtil.isEmpty(state)) {
            mAuthorizationStatus = AuthorizationStatus.ERROR;
            mAuthorizationError = new AuthorizationErrorResponse(AuthorizationError.AUTHORIZATION_FAILED,
                    "Received empty auth code and/or state.");
        } else {
            mAuthorizationStatus = AuthorizationStatus.SUCCESS;
            mAuthorizationResponse = new AuthorizationResponse(authCode, state);
        }
    }

    private AuthorizationResult(final AuthorizationStatus status, final String error, final String errorDescription) {
        if (StringUtil.isEmpty(error) || StringUtil.isEmpty(errorDescription)) {
            mAuthorizationStatus = AuthorizationStatus.ERROR;
            mAuthorizationError = new AuthorizationErrorResponse(AuthorizationError.UNKNOWN_ERROR,
                    "The error and/or errorDescription is empty.");
        } else {
            mAuthorizationStatus = status;
            mAuthorizationError = new AuthorizationErrorResponse(error, errorDescription);
        }
    }

    public static AuthorizationResult create(int resultCode, final Intent data) {
        if (null == data) {
            return new AuthorizationResult(AuthorizationStatus.ERROR,
                    AuthorizationError.AUTHORIZATION_FAILED, "Received null intent.");
        }

        switch (resultCode) {
            case AuthenticationConstants.UIResponse.BROWSER_CODE_CANCEL: //2001
                return new AuthorizationResult(AuthorizationStatus.CANCEL, AuthorizationError.USER_CANCEL,
                        "User pressed device back button to cancel the flow.");
            case AuthenticationConstants.UIResponse.BROWSER_CODE_ERROR: //2002
                return parseAuthorizationErrorResponse(data);
            case AuthenticationConstants.UIResponse.BROWSER_CODE_COMPLETE: //2003
                return parseAuthorizationResponse(data);
            default:
                return new AuthorizationResult(AuthorizationStatus.ERROR,
                        AuthorizationError.UNKNOWN_ERROR,
                        "Unknown result code [" + resultCode + "] returned from system webView/browser.");
        }
    }

    static AuthorizationResult parseAuthorizationErrorResponse(final Intent data) {
        // This is purely client side error, possible return could be chrome_not_installed or the request intent is
        // not resolvable
        final String error = data.getStringExtra(AuthenticationConstants.OAuth2.ERROR_CODE);
        final String errorDescription = data.getStringExtra(AuthenticationConstants.OAuth2.ERROR_DESCRIPTION);
        return new AuthorizationResult(AuthorizationStatus.ERROR, error, errorDescription);
    }

    static AuthorizationResult parseAuthorizationResponse(final Intent data) {
        final String url = data.getStringExtra(AuthenticationConstants.AAD.AUTHORIZATION_FINAL_URL);

        if (StringUtil.isEmpty(url)) {
            return new AuthorizationResult(AuthorizationStatus.ERROR,
                    AuthorizationError.AUTHORIZATION_FAILED, "The authorization final url is empty.");
        }

        final Map<String, String> urlParameters = StringExtensions.getUrlParameters(url);
        if (urlParameters.containsKey(AuthenticationConstants.OAuth2.CODE)) {
            if (urlParameters.containsKey(AuthenticationConstants.OAuth2.STATE)) {
                return new AuthorizationResult(AuthorizationStatus.ERROR,
                        AuthorizationError.STATE_MISMATCH, "State is not returned from server.");
            }

            final String state = urlParameters.get(AuthenticationConstants.OAuth2.STATE);
            final String code = urlParameters.get(AuthenticationConstants.OAuth2.CODE);
            return new AuthorizationResult(code, state);
        } else if (urlParameters.containsKey(AuthenticationConstants.OAuth2.ERROR)) {
            //TO-DO is the error description is required when error is returned.
            final String error = urlParameters.get(AuthenticationConstants.OAuth2.ERROR);
            final String errorDescription = urlParameters.get(AuthenticationConstants.OAuth2.ERROR_DESCRIPTION);
            return new AuthorizationResult(AuthorizationStatus.ERROR, error, errorDescription);
        } else {
            //Get invalid Server Response
            return new AuthorizationResult(AuthorizationStatus.ERROR, AuthorizationError.AUTHORIZATION_FAILED,
                    "The authorization server returned an invalid response.");
        }
    }

    /**
     * Enum for representing different authorization status.
     */
    enum AuthorizationStatus {
        /**
         * Code is successfully returned.
         */
        SUCCESS,

        /**
         * User press device back button.
         */
        CANCEL,

        /**
         * Returned URI contains error.
         */
        ERROR
    }

    /**
     * Enum for representing different authorization error.
     */
    static final class AuthorizationError {
        /**
         * Authorization failed.
         */
        static final String AUTHORIZATION_FAILED = "authorization_failed";

        /**
         * User cancelled the authorization request.
         */
        static final String USER_CANCEL = "user_cancelled";

        /**
         * Request to server failed, but result code is returned back from the service.
         */
        static final String UNKNOWN_ERROR = "unknown_error";

        static final String STATE_MISMATCH = "state_mismatch";
    }

}
