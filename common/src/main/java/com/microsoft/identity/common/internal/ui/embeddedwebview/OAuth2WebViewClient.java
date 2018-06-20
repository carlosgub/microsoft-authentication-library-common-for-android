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
package com.microsoft.identity.common.internal.ui.embeddedwebview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.ClientCertRequest;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.microsoft.identity.common.adal.internal.AuthenticationConstants;
import com.microsoft.identity.common.adal.internal.util.StringExtensions;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.providers.oauth2.AuthorizationRequest;
import com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers.NtlmChallengeHandler;
import com.microsoft.identity.common.internal.util.StringUtil;

public abstract class OAuth2WebViewClient extends WebViewClient {
    /* constants */
    private static final String TAG = OAuth2WebViewClient.class.getSimpleName();

    private final AuthorizationRequest mRequest;
    private final Context mContext;

    /**
     * @return Authorization request
     */
    public AuthorizationRequest getRequest() {
        return mRequest;
    }

    /**
     * @return context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Constructor for the OAuth2 basic web view client.
     * @param context app Context
     * @param request Authorization request
     */
    OAuth2WebViewClient(@NonNull final Context context, @NonNull final AuthorizationRequest request) {
        //the validation of redirect url and authorization request should be in upper level before launching the webview.
        mContext = context;
        mRequest = request;
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler,
                                          String host, String realm) {
        // Create a dialog to ask for creds and post it to the handler.
        Logger.info(TAG, "Receive the http auth request. Start the dialog to ask for creds. ");
        Logger.infoPII(TAG, "Host:" + host);
        //TODO TelemetryEvent.setNTLM(true);
        final NtlmChallengeHandler.Builder ntlmChanllengeHandler
                = new NtlmChallengeHandler.Builder(view, handler, host, realm);
        ntlmChanllengeHandler.setContext(mContext);
        ntlmChanllengeHandler.build().processNtlmChallenge();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        /*super.onReceivedError(view, errorCode, description, failingUrl);
        showSpinner(false);
        Logger.e(TAG, "Webview received an error. ErrorCode:" + errorCode, description,
                ADALError.ERROR_WEBVIEW);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_CODE, "Error Code:"
                + errorCode);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_MESSAGE, description);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_REQUEST_INFO, mRequest);
        sendResponse(AuthenticationConstants.UIResponse.BROWSER_CODE_ERROR, resultIntent);*/
    }


    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // Developer does not have option to control this for now
        /*super.onReceivedSslError(view, handler, error);
        showSpinner(false);
        handler.cancel();
        Logger.e(TAG, "Received ssl error. ", "", ADALError.ERROR_FAILED_SSL_HANDSHAKE);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_CODE, "Code:"
                + ERROR_FAILED_SSL_HANDSHAKE);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_MESSAGE,
                error.toString());
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_REQUEST_INFO, mRequest);
        sendResponse(AuthenticationConstants.UIResponse.BROWSER_CODE_ERROR, resultIntent);*/
    }
}
