package com.microsoft.identity.common.internal.providers.oauth2;

public class AuthorizationErrorResponse {
    // Questions, the requirement is to create the AuthorizationError object or AuthorizationException?

    private String mError;
    private String mErrorDescription;

    public AuthorizationErrorResponse(final String error, final String errorDescription) {
        mError = error;
        mErrorDescription = errorDescription;
    }

    public String getmError() {
        return mError;
    }

    public void setmError(String mError) {
        this.mError = mError;
    }

    public String getmErrorDescription() {
        return mErrorDescription;
    }

    public void setmErrorDescription(String mErrorDescription) {
        this.mErrorDescription = mErrorDescription;
    }
}
