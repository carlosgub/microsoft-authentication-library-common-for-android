package com.microsoft.identity.common.internal.cache;

import com.microsoft.identity.common.Account;
import com.microsoft.identity.common.internal.providers.oauth2.RefreshToken;
import com.microsoft.identity.common.internal.providers.oauth2.TokenRequest;

/**
 * Interface that defines methods allowing refresh token cache state to be shared between Cache Implementations
 * The assumption being that in order for a client to avoid prompting a user to sign in they need a refresh token (effectively SSO state)
 */
public interface IShareSingleSignOnState {

    void setSingleSignOnState(Account account, RefreshToken refreshToken);

    RefreshToken getSingleSignOnState(Account account, TokenRequest tr);

}
