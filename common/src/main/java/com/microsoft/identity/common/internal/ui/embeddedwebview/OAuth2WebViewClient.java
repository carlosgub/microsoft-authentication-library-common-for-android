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
import android.net.Uri;
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
    private final
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
     *
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
        super.onReceivedError(view, errorCode, description, failingUrl);
        // Create result intent when webview received an error.
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_CODE, "Error Code:"
                + errorCode);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_MESSAGE, description);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_REQUEST_INFO, mRequest);
        // Send the result back to the calling activity
        sendResponse(AuthenticationConstants.UIResponse.BROWSER_CODE_ERROR, resultIntent);
    }


    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // Developer does not have option to control this for now
        super.onReceivedSslError(view, handler, error);
        handler.cancel();
        // Webview received the ssl error and create the result intent.
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_CODE, "Code:"
                + ERROR_FAILED_SSL_HANDSHAKE);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_MESSAGE,
                error.toString());
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_REQUEST_INFO, mRequest);
        // Send the result back to the calling activity
        sendResponse(AuthenticationConstants.UIResponse.BROWSER_CODE_ERROR, resultIntent);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        // Once web view is fully loaded,set to visible
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        checkStartUrl(url);
        super.onPageStarted(view, url, favicon);
    }

    private void checkStartUrl(final String url) {
        if (StringUtil.isEmpty(url)) {
            Logger.verbose(TAG, "onPageStarted: Null url for page to load.");
            //should we send response back?
            return;
        }

        final Uri uri = Uri.parse(url);
        if (uri.isOpaque()) {
            Logger.verbose(TAG, "onPageStarted: Non-hierarchical loading uri.");
            Logger.verbosePII(TAG, "url: " + url);
            return;
        }

        Logger.verbose(TAG, "Webview starts loading.");
        if (StringUtil.isEmpty(uri.getQueryParameter(AuthenticationConstants.OAuth2.CODE))) {
            Logger.verbosePII(TAG, "Host: " + uri.getHost() + " Path: " + uri.getPath()
                    + " Auth code is returned for the loading url.");
        } else {
            Logger.verbosePII(TAG, "Host: " + uri.getHost() + " Path: " + uri.getPath()
                    + " Auth code is returned for the loading url.");
        }
    }
}
