package com.microsoft.identity.common.internal.providers.oauth2;

import android.content.Intent;
import android.net.Uri;

/**
 * A class to return the result of the authorization request to the calling code (ADAL or MSAL Controller classes)
 * This class should have a generic status in terms of : Cancelled, TimedOut, Error,  etc...
 * this class should also contain the AuthorizationResponse which contains the details returned from the
 * In the case of an error/exception this class should return the associated exception
 */
public abstract class AuthorizationResult {
    private static final String TAG = AuthorizationResult.class.getSimpleName();

    private final String mAuthCode;
    private final String mState;
    private final AuthorizationStatus mAuthorizationStatus;
    private final String mError;
    private final String mErrorDescription;


    public static AuthorizationResult create(int resultCode, final Intent data) {
        return null;
    }



    protected boolean validateAuthorizationResponse(final String returnUri) {
        final Uri responseUri = Uri.parse(returnUri);
        final String result = responseUri.getQuery();

        // if result is empty
        // throw invlid response

        return false;

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
        ERROR,

        /**
         * Authorization response is invalid.
         */
        INVALID
    }

}
