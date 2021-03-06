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
import android.support.annotation.NonNull;

import com.microsoft.identity.common.BaseAccount;
import com.microsoft.identity.common.adal.internal.util.DateExtensions;
import com.microsoft.identity.common.adal.internal.util.StringExtensions;
import com.microsoft.identity.common.internal.cache.SchemaUtil;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.providers.microsoft.azureactivedirectory.AzureActiveDirectoryIdToken;
import com.microsoft.identity.common.internal.providers.oauth2.IDToken;
import com.microsoft.identity.common.internal.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public abstract class MicrosoftAccount extends BaseAccount {

    public static final String AUTHORITY_TYPE_V1_V2 = "MSSTS";

    private static final String TAG = MicrosoftAccount.class.getSimpleName();

    private String mDisplayableId; // Legacy Identifier -  UPN (preferred) or Email
    private String mUniqueId; // Legacy Identifier - Object Id (preferred) or Subject

    private String mName;
    private String mIdentityProvider;
    private String mUid;
    private String mUtid;
    private IDToken mIDToken;
    private Uri mPasswordChangeUrl;
    private Date mPasswordExpiresOn;
    private String mTenantId; // Tenant Id of the authority that issued the idToken... not necessarily the home tenant of the account
    private String mGivenName;
    private String mFamilyName;
    private String mMiddleName;
    private String mEnvironment;

    /**
     * Constructor of MicrosoftAccount.
     */
    public MicrosoftAccount() {
        super();
        Logger.verbose(TAG, "Init: " + TAG);
    }

    /**
     * Constructor of MicrosoftAccount.
     *
     * @param idToken id token of the Microsoft account.
     * @param uid     UID of the Microsoft account.
     * @param utid    UTID of the Microsoft account.
     */
    public MicrosoftAccount(@NonNull final IDToken idToken,
                            final String uid,
                            final String utid) {
        Logger.verbose(TAG, "Init: " + TAG);
        mIDToken = idToken;
        final Map<String, String> claims = idToken.getTokenClaims();
        mUniqueId = getUniqueId(claims);
        mDisplayableId = getDisplayableId(claims);
        mName = claims.get(AzureActiveDirectoryIdToken.NAME);
        mIdentityProvider = claims.get(AzureActiveDirectoryIdToken.ISSUER);
        mGivenName = claims.get(AzureActiveDirectoryIdToken.GIVEN_NAME);
        mFamilyName = claims.get(AzureActiveDirectoryIdToken.FAMILY_NAME);
        mMiddleName = claims.get(AzureActiveDirectoryIdToken.MIDDLE_NAME);
        if (!StringUtil.isEmpty(claims.get(AzureActiveDirectoryIdToken.TENANT_ID))) {
            mTenantId = claims.get(AzureActiveDirectoryIdToken.TENANT_ID);
        } else if (!StringUtil.isEmpty(utid)) {
            Logger.warnPII(TAG, "realm is not returned from server. Use utid as realm.");
            mTenantId = utid;
        } else {
            // According to the spec, full tenant or organizational identifier that account belongs to.
            // Can be an empty string for non-AAD scenarios.
            Logger.warnPII(TAG, "realm and utid is not returned from server. Use empty string as default tid.");
            mTenantId = "";
        }

        mUid = uid;
        mUtid = utid;

        long mPasswordExpiration = 0;

        if (!StringExtensions.isNullOrBlank(claims.get(AzureActiveDirectoryIdToken.PASSWORD_EXPIRATION))) {
            mPasswordExpiration = Long.parseLong(claims.get(AzureActiveDirectoryIdToken.PASSWORD_EXPIRATION));
        }

        if (mPasswordExpiration > 0) {
            // pwd_exp returns seconds to expiration time
            // it returns in seconds. Date accepts milliseconds.
            Calendar expires = new GregorianCalendar();
            expires.add(Calendar.SECOND, (int) mPasswordExpiration);
            mPasswordExpiresOn = expires.getTime();
        }

        mPasswordChangeUrl = null;
        if (!StringExtensions.isNullOrBlank(claims.get(AzureActiveDirectoryIdToken.PASSWORD_CHANGE_URL))) {
            mPasswordChangeUrl = Uri.parse(claims.get(AzureActiveDirectoryIdToken.PASSWORD_CHANGE_URL));
        }
    }

    protected abstract String getDisplayableId(final Map<String, String> claims);

    private String getUniqueId(final Map<String, String> claims) {
        final String methodName = "getUniqueId";

        String uniqueId = null;

        if (!StringExtensions.isNullOrBlank(claims.get(AzureActiveDirectoryIdToken.OJBECT_ID))) {
            Logger.info(TAG + ":" + methodName, "Using ObjectId as uniqueId");
            uniqueId = claims.get(AzureActiveDirectoryIdToken.OJBECT_ID);
        } else if (!StringExtensions.isNullOrBlank(claims.get(AzureActiveDirectoryIdToken.SUBJECT))) {
            Logger.info(TAG + ":" + methodName, "Using Subject as uniqueId");
            uniqueId = claims.get(AzureActiveDirectoryIdToken.SUBJECT);
        }

        return uniqueId;
    }

    /**
     * @param givenName given name of the Microsoft account.
     */
    public void setFirstName(final String givenName) {
        mGivenName = givenName;
    }

    /**
     * @param familyName family name of the Microsoft account.
     */
    public void setFamilyName(final String familyName) {
        mFamilyName = familyName;
    }

    /**
     * @return The displayable value in the UserPrincipleName(UPN) format. Can be null if not
     * returned from the service.
     */
    public String getDisplayableId() {
        return mDisplayableId;
    }

    /**
     * Sets the displayableId of a user when making acquire token API call.
     *
     * @param displayableId displayable ID.
     */
    public void setDisplayableId(final String displayableId) {
        mDisplayableId = displayableId;
    }

    /**
     * @return The identity provider of the user authenticated. Can be null if not returned from the service.
     */
    public String getIdentityProvider() {
        return mIdentityProvider;
    }

    /**
     * @return The unique identifier of the user, which is across tenant.
     */
    public String getUserId() {
        return mUniqueId;
    }

    /**
     * Gets the uid.
     *
     * @return The uid to get.
     */
    public String getUid() {
        return mUid;
    }

    /**
     * Sets the uid.
     *
     * @param uid The uid to set.
     */
    public void setUid(final String uid) {
        mUid = uid;
    }

    /**
     * Sets the utid.
     *
     * @param uTid The utid to set.
     */
    public void setUtid(final String uTid) {
        mUtid = uTid;
    }

    /**
     * Sets the name.
     *
     * @param name The name to set.
     */
    public void setName(final String name) {
        mName = name;
    }

    /**
     * Sets the identity provider.
     *
     * @param idp The identity provider to set.
     */
    public void setIdentityProvider(final String idp) {
        mIdentityProvider = idp;
    }

    /**
     * Gets the utid.
     *
     * @return The utid to get.
     */
    public String getUtid() {
        return mUtid;
    }

    void setUserId(final String userid) {
        mUniqueId = userid;
    }

    /**
     * Return the unique identifier for the account...
     *
     * @return unique identifier string.
     */
    @Override
    public String getUniqueIdentifier() {
        return StringExtensions.base64UrlEncodeToString(mUid) + "." + StringExtensions.base64UrlEncodeToString(mUtid);
    }

    @Override
    public List<String> getCacheIdentifiers() {
        List<String> cacheIdentifiers = new ArrayList<>();

        if (mDisplayableId != null) {
            cacheIdentifiers.add(mDisplayableId);
        }

        if (mUniqueId != null) {
            cacheIdentifiers.add(mUniqueId);
        }

        if (getUniqueIdentifier() != null) {
            cacheIdentifiers.add(getUniqueIdentifier());
        }

        return cacheIdentifiers;
    }

    /**
     * Gets password change url.
     *
     * @return the password change uri.
     */
    public Uri getPasswordChangeUrl() {
        return mPasswordChangeUrl;
    }

    /**
     * Gets password expires on.
     *
     * @return the time when the password will expire.
     */
    public Date getPasswordExpiresOn() {
        return DateExtensions.createCopy(mPasswordExpiresOn);
    }

    /**
     * @return mIDToken of the Microsoft account.
     */
    public IDToken getIDToken() {
        return mIDToken;
    }

    @Override
    public String getHomeAccountId() {
        // TODO -- This method's functionality is duplicative of
        // Account#getUniqueIdentifier except that that implementation
        // was coded for the refactored ADAL cache which expects
        // uid/utid to be base64 encoded for legacy support.
        return getUid() + "." + getUtid();
    }

    public void setEnvironment(final String environment) {
        mEnvironment = environment;
    }

    @Override
    public String getEnvironment() {
        return mEnvironment;
    }

    @Override
    public String getRealm() {
        return mTenantId;
    }

    @Override
    public String getLocalAccountId() {
        return getUserId();
    }

    @Override
    public String getUsername() {
        return getDisplayableId();
    }

    @Override
    public String getAlternativeAccountId() {
        return SchemaUtil.getAlternativeAccountId(mIDToken);
    }

    @Override
    public String getFirstName() {
        return mGivenName;
    }

    @Override
    public String getFamilyName() {
        return mFamilyName;
    }

    /**
     * @return The given name of the user. Can be null if not returned from the service.
     */
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getMiddleName() {
        return mMiddleName;
    }

    @Override
    public String getAvatarUrl() {
        return SchemaUtil.getAvatarUrl(mIDToken);
    }

    //CHECKSTYLE:OFF
    @Override
    public String toString() {
        return "MicrosoftAccount{" +
                "mDisplayableId='" + mDisplayableId + '\'' +
                ", mUniqueId='" + mUniqueId + '\'' +
                ", mName='" + mName + '\'' +
                ", mIdentityProvider='" + mIdentityProvider + '\'' +
                ", mUid='" + mUid + '\'' +
                ", mUtid='" + mUtid + '\'' +
                ", mIDToken=" + mIDToken +
                ", mPasswordChangeUrl=" + mPasswordChangeUrl +
                ", mPasswordExpiresOn=" + mPasswordExpiresOn +
                ", mTenantId='" + mTenantId + '\'' +
                ", mGivenName='" + mGivenName + '\'' +
                ", mFamilyName='" + mFamilyName + '\'' +
                "} " + super.toString();
    }
    //CHECKSTYLE:ON
}
