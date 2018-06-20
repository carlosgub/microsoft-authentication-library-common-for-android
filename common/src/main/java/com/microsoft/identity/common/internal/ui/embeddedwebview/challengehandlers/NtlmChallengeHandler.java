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
package com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebView;
import android.widget.EditText;

import com.microsoft.identity.common.R;
import com.microsoft.identity.common.adal.internal.AuthenticationConstants;
import com.microsoft.identity.common.internal.logging.Logger;

/**
 * Http auth handler for NTLM challenge on web view.
 */
public final class NtlmChallengeHandler {
    private static final String TAG = NtlmChallengeHandler.class.getSimpleName();
    private HttpAuthHandler mHandler;
    private WebView mView;
    private String mHost;
    private String mRealm;
    private Context mContext;
    private Activity mActivity;

    NtlmChallengeHandler(@NonNull final HttpAuthHandler handler,
                         @NonNull final WebView view,
                         @NonNull final String host,
                         @NonNull final String realm,
                         final Context context,
                         final Activity activity) {
        mHandler = handler;
        mView = view;
        mHost = host;
        mRealm = realm;
        mContext = context;
        mActivity = activity;
    }

    public void processNtlmChallenge() {
        if (mHandler.useHttpAuthUsernamePassword() && mView != null) {
            String[] haup = mView.getHttpAuthUsernamePassword(mHost, mRealm);
            if (haup != null && haup.length == 2) {
                final String userName = haup[0];
                final String password = haup[0];
                if (userName != null && password != null) {
                    mHandler.proceed(userName, password);
                }
            }
        } else {
            showHttpAuthDialog();
        }
    }

    private void showHttpAuthDialog() {
        final LayoutInflater factory = LayoutInflater.from(mContext);
        final View v = factory.inflate(mContext.getResources().getLayout(R.layout.http_auth_dialog), null);
        final EditText usernameView = (EditText) v.findViewById(R.id.editUserName);
        final EditText passwordView = (EditText) v.findViewById(R.id.editPassword);
        final String title = mContext.getText(R.string.http_auth_dialog_title).toString();
        final AlertDialog.Builder httpAuthDialog = new AlertDialog.Builder(mContext);
        httpAuthDialog.setTitle(title)
                .setView(v)
                .setPositiveButton(R.string.http_auth_dialog_login,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mHandler.proceed(usernameView.getText().toString(), passwordView.getText().toString());
                            }
                        })
                .setNegativeButton(R.string.http_auth_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mHandler.cancel();
                                cancelRequest();
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                mHandler.cancel();
                                cancelRequest();
                            }
                        }).create().show();
    }

    private void cancelRequest() {
        Logger.verbose(TAG, "Sending intent to cancel authentication activity");
        Intent resultIntent = new Intent();
        //data.putExtra(AuthenticationConstants.Browser.REQUEST_ID, mAuthRequest.getRequestId());
        mActivity.setResult(AuthenticationConstants.UIResponse.BROWSER_CODE_CANCEL, resultIntent);
        mActivity.finish();
    }

    public static class Builder {
        private HttpAuthHandler mHandler;
        private WebView mView;
        private String mHost;
        private String mRealm;
        private Context mContext;
        private Activity mActivity;

        public Builder(final WebView view,
                       final HttpAuthHandler handler,
                       final String host,
                       final String realm) {
            mHandler = handler;
            mView = view;
            mHost = host;
            mRealm = realm;
        }

        public void setContext(final Context context) {
            mContext = context;
        }

        public void setActivity(final Activity activity) {
            mActivity = activity;
        }

        public NtlmChallengeHandler build() {
            return new NtlmChallengeHandler(mHandler, mView, mHost, mRealm, mContext, mActivity);
        }
    }
}



