package com.microsoft.identity.common.internal.ui.embeddedwebview.challengehandlers;

import android.content.Intent;

public abstract class ChallengeCompletionCallback {
    //private int requestCode;
    private int mResultCode;
    private Intent mData;

    public ChallengeCompletionCallback() {
    }

    public abstract void sendResponse(final int returnCode, Intent responseIntent);
}
