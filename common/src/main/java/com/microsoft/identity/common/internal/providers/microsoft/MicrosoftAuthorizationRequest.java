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
package com.microsoft.identity.common.internal.providers.microsoft;

import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import com.google.gson.annotations.SerializedName;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.net.ObjectMapper;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationRequest;
import com.microsoft.identity.common.internal.providers.oauth2.PkceChallenge;
import com.microsoft.identity.common.internal.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.UUID;

public abstract class MicrosoftAuthorizationRequest<T extends MicrosoftAuthorizationRequest<T>> extends AuthorizationRequest<T> {
    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 6873634931996113294L;

    private static final String TAG = MicrosoftAuthorizationRequest.class.getSimpleName();

    /**
     * Required.
     */
    private transient URL mAuthority; //Not going to be serialized into request url parameters

    /**
     * Can be used to pre-fill the username/email address field of the sign-in page for the user, if you know their username ahead of time.
     */
    @SerializedName("login_hint")
    private String mLoginHint;
    /**
     * Correlation ID.
     */
    @SerializedName("client-request-id")
    private UUID mCorrelationId;
    /**
     * Used to secure authorization code grants via Proof Key for Code Exchange (PKCE) from a native client.
     */
    @SerializedName("pkceChallenge")
    private PkceChallenge mPkceChallenge;
    /**
     * Extra query parameters.
     */
    private transient String mExtraQueryParam; //TODO need valid the format and append it into the start url
    /**
     * The version of the calling library.
     */
    @SerializedName("x-client-Ver")
    private String mLibraryVersion;

    @SerializedName("x-client-SKU")
    private String mLibraryName;

    @SerializedName("x-client-OS")
    private String mDiagnosticOS;

    @SerializedName("x-client-CPU")
    private String mDiagnosticCPU;

    @SerializedName("x-client-DM")
    private String mDiagnosticDM;


    /**
     * Constructor of MicrosoftAuthorizationRequest.
     */
    protected MicrosoftAuthorizationRequest(final Builder builder) {
        super(builder);
        mAuthority = builder.mAuthority;
        mLoginHint = builder.mLoginHint;
        mCorrelationId = builder.mCorrelationId;

        mExtraQueryParam = builder.mExtraQueryParam;
        mPkceChallenge = PkceChallenge.newPkceChallenge();
        mState = generateEncodedState();

        //Initialize the diagnostic properties.

        //TODO: Need to figure out how to flow this information down
        //builder.setLibraryVersion(PublicClientApplication.getSdkVersion());
        mLibraryVersion = "0.1.3";
        mLibraryName = "MSAL.Android";
        mDiagnosticOS = String.valueOf(Build.VERSION.SDK_INT);
        mDiagnosticDM = android.os.Build.MODEL;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mDiagnosticCPU = Build.CPU_ABI;
        } else {
            final String[] supportedABIs = Build.SUPPORTED_ABIS;
            if (supportedABIs != null && supportedABIs.length > 0) {
                mDiagnosticCPU = supportedABIs[0];
            }
        }
    }


    public abstract static class Builder<B extends MicrosoftAuthorizationRequest.Builder<B>> extends AuthorizationRequest.Builder<B> {
        /**
         * Required.
         */
        private URL mAuthority;
        /**
         * Used to secure authorization code grants via Proof Key for Code Exchange (PKCE) from a native client.
         */
        private PkceChallenge mPkceChallenge;
        /**
         * The version of the calling library.
         */
        private String mLibraryVersion;

        /**
         * The name of the calling library.
         */
        private String mLibraryName;

        public Builder() {
        }

        public B setAuthority(URL authority) {
            mAuthority = authority;
            return self();
        }

        public B setPkceChallenge(PkceChallenge pkceChallenge) {
            mPkceChallenge = pkceChallenge;
            return self();
        }

        public B setLibraryVersion(String libraryVersion) {
            mLibraryVersion = libraryVersion;
            return self();
        }

        public B setLibraryName(String libraryName) {
            mLibraryName = libraryName;
            return self();
        }

        public abstract B self();

    }

    public URL getAuthority() {
        return mAuthority;
    }

    public String getLoginHint() {
        return mLoginHint;
    }

    public UUID getCorrelationId() {
        return mCorrelationId;
    }

    public PkceChallenge getPkceChallenge() {
        return mPkceChallenge;
    }

    public String getExtraQueryParam() {
        return mExtraQueryParam;
    }

    public String getLibraryVersion() {
        return mLibraryVersion;
    }

    public String getLibraryName() {
        return mLibraryName;
    }

    public String getDiagnosticOS() {
        return mDiagnosticOS;
    }

    public String getDiagnosticCPU() {
        return mDiagnosticCPU;
    }

    public String getDiagnosticDM() {
        return mDiagnosticDM;
    }


    @Override
    public Uri getAuthorizationRequestAsHttpRequest() throws UnsupportedEncodingException {
        Uri.Builder uriBuilder = Uri.parse(getAuthorizationEndpoint()).buildUpon();
        for (Map.Entry<String, String> entry : ObjectMapper.serializeObjectHashMap(this).entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : ObjectMapper.deserializeQueryStringToMap(getExtraQueryParam()).entrySet()) {
            uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue());
        }

        return uriBuilder.build();
    }
}
