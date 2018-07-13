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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.webkit.ClientCertRequest;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import com.microsoft.identity.common.adal.internal.AuthenticationConstants;
import com.microsoft.identity.common.adal.internal.util.StringExtensions;
import com.microsoft.identity.common.exception.ClientException;
import com.microsoft.identity.common.exception.ErrorStrings;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.providers.microsoft.MicrosoftAuthorizationRequest;
import com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers.ClientCertAuthChallengeHandler;
import com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers.IChallengeCompletionCallback;
import com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers.PKeyAuthChallenge;
import com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers.PKeyAuthChallengeHandler;
import com.microsoft.identity.common.internal.util.StringUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * For web view client, we do not distinguish V1 from V2.
 * Thus we name V1 and V2 webview client as AADWebViewClient, synced with the naming in the iOS common library.
 * <p>
 * The only differences between V1 and V2 is
 * 1. on the start url construction, which is handled in the Authorization request classes.
 * 2. the auth result is handled in the Authorization result classes.
 */
public class AzureActiveDirectoryWebViewClient extends OAuth2WebViewClient {
    private static final String TAG = AzureActiveDirectoryWebViewClient.class.getSimpleName();
    private static final String INSTALL_URL_KEY = "app_link";
    public static final String ERROR = "error";
    public static final String ERROR_DESCRIPTION = "error_description";

    public AzureActiveDirectoryWebViewClient(@NonNull final Activity activity,
                                      @NonNull final MicrosoftAuthorizationRequest request,
                                      @NonNull final IChallengeCompletionCallback callback) {
        super(activity, request, callback);
    }

    /**
     * Give the host application a chance to take over the control when a new url is about to be loaded in the current WebView.
     * This method was deprecated in API level 24.
     *
     * @param view The WebView that is initiating the callback.
     * @param url  The url to be loaded.
     * @return return true means the host application handles the url, while return false means the current WebView handles the url.
     */
    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
        if (StringUtil.isEmpty(url)) {
            throw new IllegalArgumentException("Redirect to empty url in web view.");
        }
        return handleUrl(view, url);
    }

    /**
     * Give the host application a chance to take over the control when a new url is about to be loaded in the current WebView.
     * This method is added in API level 24.
     *
     * @param view    The WebView that is initiating the callback.
     * @param request Object containing the details of the request.
     * @return return true means the host application handles the url, while return false means the current WebView handles the url.
     */
    @Override
    @RequiresApi(Build.VERSION_CODES.N)
    public boolean shouldOverrideUrlLoading(final WebView view, final WebResourceRequest request) {
        final Uri requestUrl = request.getUrl();
        return handleUrl(view, requestUrl.toString());
    }

    private boolean handleUrl(final WebView view, final String url) {
        final String formattedURL = url.toLowerCase(Locale.US);

        if (isPkeyAuthUrl(formattedURL)) {
            Logger.verbose(TAG, "WebView detected request for pkeyauth challenge.");
            try {
                final PKeyAuthChallenge pKeyAuthChallenge = new PKeyAuthChallenge(formattedURL);
                final PKeyAuthChallengeHandler pKeyAuthChallengeHandler = new PKeyAuthChallengeHandler(view, getRequest(), getCompletionCallback());
                pKeyAuthChallengeHandler.processChallenge(pKeyAuthChallenge);
            } catch (final ClientException exception) {
                Logger.error(TAG, exception.getErrorCode(), null);
                Logger.errorPII(TAG, exception.getMessage(), exception);
                returnError(exception.getErrorCode(), exception.getMessage());
                view.stopLoading();
            }
            return true;
        } else if (isRedirectUrl(formattedURL)) {
            Logger.verbose(TAG, "Navigation starts with the redirect uri.");
            return processRedirectUrl(view, formattedURL);
        } else if (isWebsiteRequestUrl(formattedURL)) {
            Logger.verbose(TAG, "It is an external website request");
            return processWebsiteRequest(view, formattedURL);
        } else if (isInstallRequestUrl(formattedURL)) {
            Logger.verbose(TAG, "It is an install request");
            return processInstallRequest(view, formattedURL);
        } else {
            Logger.verbose(TAG, "It is an invalid redirect uri.");
            return processInvalidUrl(view, url);
        }
    }

    private boolean isPkeyAuthUrl(@NonNull final String url) {
        return url.startsWith(AuthenticationConstants.Broker.PKEYAUTH_REDIRECT);
    }

    private boolean isRedirectUrl(@NonNull final String url) {
        return url.startsWith(getRequest().getRedirectUri().toLowerCase(Locale.US));
    }

    private boolean isWebsiteRequestUrl(@NonNull final String url) {
        return url.startsWith(AuthenticationConstants.Broker.BROWSER_EXT_PREFIX);
    }

    private boolean isInstallRequestUrl(@NonNull final String url) {
        return url.startsWith(AuthenticationConstants.Broker.BROWSER_EXT_INSTALL_PREFIX);
    }

    private boolean isBrokerRequest(final Intent callingIntent) {
        // Intent should have a flag and activity is hosted inside broker
        return callingIntent != null
                && !StringExtensions.isNullOrBlank(callingIntent
                .getStringExtra(AuthenticationConstants.Broker.BROKER_REQUEST));
    }

    private boolean processRedirectUrl(@NonNull final WebView view, @NonNull final String url) {
        final Map<String, String> parameters = StringExtensions.getUrlParameters(url);
        if (!StringExtensions.isNullOrBlank(parameters.get(ERROR))) {
            Logger.info(TAG, "Sending intent to cancel authentication activity");
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_CODE, parameters.get(ERROR));
            resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_MESSAGE, parameters.get(ERROR_DESCRIPTION));
            view.stopLoading();
            getCompletionCallback().onChallengeResponseReceived(AuthenticationConstants.UIResponse.BROWSER_CODE_CANCEL, resultIntent);

        } else {
            Logger.verbose(TAG, "It is pointing to redirect. Final url can be processed to get the code or error.");
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AuthenticationConstants.Browser.AUTHORIZATION_FINAL_URL, url);
            resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_REQUEST_INFO,
                    getRequest());
            view.stopLoading();
            getCompletionCallback().onChallengeResponseReceived(
                    AuthenticationConstants.UIResponse.BROWSER_CODE_COMPLETE,
                    resultIntent);
            //the TokenTask should be processed at after the authorization process in the upper calling layer.
        }

        return true;
    }

    private boolean processWebsiteRequest(@NonNull final WebView view, @NonNull final String url) {
        //Open url link in browser
        final String link = url
                .replace(AuthenticationConstants.Broker.BROWSER_EXT_PREFIX, "https://");
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        getActivity().getApplicationContext().startActivity(intent);
        view.stopLoading();
        Intent resultIntent = new Intent();
        getCompletionCallback().onChallengeResponseReceived(AuthenticationConstants.UIResponse.BROWSER_CODE_CANCEL, resultIntent);
        return true;
    }

    private boolean processInstallRequest(@NonNull final WebView view, @NonNull final String url) {
        Logger.verbose(TAG, "Return to caller with BROKER_REQUEST_RESUME, and waiting for result.");
        final Intent resultIntent = new Intent();
        getCompletionCallback().onChallengeResponseReceived(AuthenticationConstants.UIResponse.BROKER_REQUEST_RESUME, resultIntent);

        // Having thread sleep for 1 second for calling activity to receive the result from
        // prepareForBrokerResumeRequest, thus the receiver for listening broker result return
        // can be registered. openLinkInBrowser will launch activity for going to
        // play store and broker app download page which brought the calling activity down
        // in the activity stack.

        Logger.verbose(TAG, "Error occurred when having thread sleeping for 1 second.");
        final Handler handler = new Handler();
        final int threadSleepForCallingActivity = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> parameters = StringExtensions.getUrlParameters(url);
                String link = parameters.get(INSTALL_URL_KEY)
                        .replace(AuthenticationConstants.Broker.BROWSER_EXT_PREFIX, "https://");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                getActivity().getApplicationContext().startActivity(intent);
                view.stopLoading();
            }
        }, threadSleepForCallingActivity);

        return true;
    }

    private boolean processInvalidUrl(@NonNull final WebView view, @NonNull final String url) {
        if (isBrokerRequest(getActivity().getIntent())
                && url.startsWith(AuthenticationConstants.Broker.REDIRECT_PREFIX)) {
            Logger.error(TAG, "The RedirectUri is not as expected.", null);
            Logger.errorPII(TAG, String.format("Received %s and expected %s", url, getRequest().getRedirectUri()), null);
            returnError(ErrorStrings.DEVELOPER_REDIRECTURI_INVALID,
                    String.format("The RedirectUri is not as expected. Received %s and expected %s", url,
                            getRequest().getRedirectUri()));
            view.stopLoading();
            return true;
        }

        if (url.toLowerCase(Locale.US).equals("about:blank")) {
            Logger.verbose(TAG, "It is an blank page request");
            return true;
        }

        if (!url.toLowerCase(Locale.US).startsWith(AuthenticationConstants.Broker.REDIRECT_SSL_PREFIX)) {
            Logger.error(TAG, "The webView was redirected to an unsafe URL.", null);
            returnError(ErrorStrings.WEBVIEW_REDIRECTURL_NOT_SSL_PROTECTED, "The webView was redirected to an unsafe URL.");
            view.stopLoading();
            return true;
        }

        return false;
    }

    private void returnError(final String errorCode, final String errorMessage) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_CODE, errorCode);
        resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_ERROR_MESSAGE, errorMessage);

        if (getRequest() != null) {
            if (getRequest() instanceof MicrosoftAuthorizationRequest) {
                resultIntent.putExtra(AuthenticationConstants.Browser.REQUEST_ID, ((MicrosoftAuthorizationRequest) getRequest()).getCorrelationId());
            }

            resultIntent.putExtra(AuthenticationConstants.Browser.RESPONSE_REQUEST_INFO, getRequest());
        }

        getCompletionCallback().onChallengeResponseReceived(AuthenticationConstants.UIResponse.BROWSER_CODE_ERROR, resultIntent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedClientCertRequest(WebView view, final ClientCertRequest clientCertRequest) {
        final ClientCertAuthChallengeHandler clientCertAuthChallengeHandler = new ClientCertAuthChallengeHandler(getActivity());
        clientCertAuthChallengeHandler.processChallenge(clientCertRequest);
    }
}