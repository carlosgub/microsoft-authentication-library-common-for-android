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
package com.microsoft.identity.common;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.microsoft.identity.common.adal.internal.AndroidSecretKeyEnabledHelper;
import com.microsoft.identity.common.adal.internal.cache.StorageHelper;
import com.microsoft.identity.common.internal.cache.AccountCredentialCache;
import com.microsoft.identity.common.internal.cache.CacheKeyValueDelegate;
import com.microsoft.identity.common.internal.cache.SharedPreferencesFileManager;
import com.microsoft.identity.common.internal.dto.AccessTokenRecord;
import com.microsoft.identity.common.internal.dto.AccountRecord;
import com.microsoft.identity.common.internal.dto.Credential;
import com.microsoft.identity.common.internal.dto.CredentialType;
import com.microsoft.identity.common.internal.dto.IdTokenRecord;
import com.microsoft.identity.common.internal.dto.RefreshTokenRecord;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.microsoft.identity.common.internal.cache.CacheKeyValueDelegate.CACHE_VALUE_SEPARATOR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AccountCredentialCacheTest extends AndroidSecretKeyEnabledHelper {

    static final String HOME_ACCOUNT_ID = "29f3807a-4fb0-42f2-a44a-236aa0cb3f97.0287f963-2d72-4363-9e3a-5705c5b0f031";
    static final String ENVIRONMENT = "login.microsoftonline.com";
    static final String CLIENT_ID = "0287f963-2d72-4363-9e3a-5705c5b0f031";
    static final String TARGET = "user.read user.write https://graph.windows.net";
    // In the case of AAD, the realm is the tenantId
    static final String REALM = "3c62ac97-29eb-4aed-a3c8-add0298508d";
    static final String MIDDLE_NAME = "Q";
    static final String NAME = "Jane Doe";
    static final String LOCAL_ACCOUNT_ID = "00000000-0000-0000-088f-0e042cc22ac0";
    static final String USERNAME = "user.foo@tenant.onmicrosoft.com";
    static final String AUTHORITY_TYPE = "MSSTS";
    static final String CACHED_AT = "0";
    static final String EXPIRES_ON = "0";
    static final String SECRET = "3642fe2f-2c46-4824-9f27-e44b0e3e1278";

    private static final String ENVIRONMENT_LEGACY = "login.windows.net";
    private static final String REALM2 = "20d3e9fa-982a-40bc-bea4-26bbe3fd332e";
    private static final String REALM3 = "fc5171ec-2889-4ba6-bd1f-216fe87a8613";

    // The names of the SharedPreferences file on disk - must match AccountCredentialCache declaration to test impl
    private static final String sAccountCredentialSharedPreferences =
            "com.microsoft.identity.client.account_credential_cache";

    private AccountCredentialCache mAccountCredentialCache;
    private CacheKeyValueDelegate mDelegate;
    private SharedPreferencesFileManager mSharedPreferencesFileManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final Context testContext = InstrumentationRegistry.getTargetContext();
        mDelegate = new CacheKeyValueDelegate();
        mSharedPreferencesFileManager = new SharedPreferencesFileManager(
                testContext,
                sAccountCredentialSharedPreferences,
                new StorageHelper(testContext) // Use encrypted storage for tests...
        );
        mAccountCredentialCache = new AccountCredentialCache(
                mDelegate,
                mSharedPreferencesFileManager
        );
    }

    @After
    public void tearDown() {
        // Wipe the SharedPreferences between tests...
        mAccountCredentialCache.clearAll();
    }

    @Test
    public void saveAccount() {
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        account.setMiddleName(MIDDLE_NAME);
        account.setName(NAME);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Synthesize a cache key for it
        final String accountCacheKey = mDelegate.generateCacheKey(account);

        // Resurrect the Account
        final AccountRecord restoredAccount = mAccountCredentialCache.getAccount(accountCacheKey);
        assertTrue(account.equals(restoredAccount));
    }

    @Test
    public void saveAccountNoRealm() {
        final AccountRecord account = new AccountRecord();
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Synthesize a cache key for it
        final String accountCacheKey = mDelegate.generateCacheKey(account);

        // Resurrect the Account
        final AccountRecord restoredAccount = mAccountCredentialCache.getAccount(accountCacheKey);
        assertTrue(account.equals(restoredAccount));
    }

    @Test
    public void saveAccountNoHomeAccountIdNoRealm() {
        final AccountRecord account = new AccountRecord();
        account.setEnvironment(ENVIRONMENT);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Synthesize a cache key for it
        final String accountCacheKey = mDelegate.generateCacheKey(account);

        // Resurrect the Account
        final AccountRecord restoredAccount = mAccountCredentialCache.getAccount(accountCacheKey);
        assertTrue(account.equals(restoredAccount));
    }

    @Test
    public void saveIdToken() {
        final IdTokenRecord idToken = new IdTokenRecord();
        idToken.setHomeAccountId(HOME_ACCOUNT_ID);
        idToken.setEnvironment(ENVIRONMENT);
        idToken.setRealm(REALM);
        idToken.setCredentialType(CredentialType.IdToken.name());
        idToken.setClientId(CLIENT_ID);
        idToken.setSecret(SECRET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(idToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(idToken);

        // Resurrect the Credential
        final Credential restoredIdToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertEquals(idToken.getHomeAccountId(), restoredIdToken.getHomeAccountId());
        assertEquals(idToken.getEnvironment(), restoredIdToken.getEnvironment());
        assertEquals(idToken.getCredentialType(), restoredIdToken.getCredentialType());
        assertEquals(idToken.getClientId(), restoredIdToken.getClientId());
        assertTrue(idToken.equals(restoredIdToken));
    }

    @Test
    public void saveCredential() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(refreshToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(refreshToken);

        // Resurrect the Credential
        final Credential restoredRefreshToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(refreshToken.equals(restoredRefreshToken));
    }

    @Test
    public void saveCredentialNoTarget() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(refreshToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(refreshToken);

        // Resurrect the Credential
        final Credential restoredRefreshToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(refreshToken.equals(restoredRefreshToken));
    }

    @Test
    public void saveCredentialNoRealmNoTarget() {
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);

        // Save the Credential
        mAccountCredentialCache.saveCredential(accessToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(accessToken);

        // Resurrect the Credential
        final Credential restoredAccessToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(accessToken.equals(restoredAccessToken));
    }

    @Test
    public void saveCredentialNoHomeAccountIdNoRealmNoTarget() {
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(accessToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(accessToken);

        // Resurrect the Credential
        final Credential restoredAccessToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(accessToken.equals(restoredAccessToken));
    }

    @Test
    public void saveCredentialNoHomeAccountId() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(refreshToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(refreshToken);

        // Resurrect the Credential
        final Credential restoredRefreshToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(refreshToken.equals(restoredRefreshToken));
    }

    @Test
    public void saveCredentialNoHomeAccountIdNoRealm() {
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(accessToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(accessToken);

        // Resurrect the Credential
        final Credential restoredAccessToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(accessToken.equals(restoredAccessToken));
    }

    @Test
    public void saveCredentialNoHomeAccountIdNoTarget() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(refreshToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(refreshToken);

        // Resurrect the Credential
        final Credential restoredRefreshToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertEquals(refreshToken, restoredRefreshToken);
    }

    @Test
    public void saveCredentialNoRealm() {
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        // Save the Credential
        mAccountCredentialCache.saveCredential(accessToken);

        // Synthesize a cache key for it
        final String credentialCacheKey = mDelegate.generateCacheKey(accessToken);

        // Resurrect the Credential
        final Credential restoredAccessToken = mAccountCredentialCache.getCredential(credentialCacheKey);
        assertTrue(accessToken.equals(restoredAccessToken));
    }

    @Test
    public void getAccounts() {
        // Save an Account into the cache
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        mAccountCredentialCache.saveAccount(account);

        // Save an AccessToken into the cache
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm("Foo");
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        mAccountCredentialCache.saveCredential(accessToken);

        // Save a RefreshToken into the cache
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);
        mAccountCredentialCache.saveCredential(refreshToken);

        // Verify getAccountsFilteredBy() returns one matching element
        final List<AccountRecord> accounts = mAccountCredentialCache.getAccounts();
        assertTrue(accounts.size() == 1);
        assertEquals(account, accounts.get(0));
    }

    @Test
    public void getAccountsNullEnvironment() {
        final AccountRecord account1 = new AccountRecord();
        account1.setHomeAccountId(HOME_ACCOUNT_ID);
        account1.setEnvironment(ENVIRONMENT);
        account1.setRealm(REALM);
        account1.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account1.setUsername(USERNAME);
        account1.setAuthorityType(AUTHORITY_TYPE);

        final AccountRecord account2 = new AccountRecord();
        account2.setHomeAccountId(HOME_ACCOUNT_ID);
        account2.setEnvironment(ENVIRONMENT_LEGACY);
        account2.setRealm(REALM);
        account2.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account2.setUsername(USERNAME);
        account2.setAuthorityType(AUTHORITY_TYPE);

        // Save the Accounts
        mAccountCredentialCache.saveAccount(account1);
        mAccountCredentialCache.saveAccount(account2);

        // Test retrieval
        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(
                HOME_ACCOUNT_ID,
                null,
                REALM
        );
        assertEquals(2, accounts.size());
    }

    @Test
    public void getAccountsComplete() {
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Test retrieval
        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(HOME_ACCOUNT_ID, ENVIRONMENT, REALM);
        assertEquals(1, accounts.size());
        final AccountRecord retrievedAccount = accounts.get(0);
        assertEquals(HOME_ACCOUNT_ID, retrievedAccount.getHomeAccountId());
        assertEquals(ENVIRONMENT, retrievedAccount.getEnvironment());
        assertEquals(REALM, retrievedAccount.getRealm());
    }

    @Test
    public void getAccountsNoHomeAccountId() {
        final AccountRecord account = new AccountRecord();
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Test retrieval
        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(null, ENVIRONMENT, REALM);
        assertEquals(1, accounts.size());
        final AccountRecord retrievedAccount = accounts.get(0);
        assertEquals(HOME_ACCOUNT_ID, retrievedAccount.getHomeAccountId());
        assertEquals(ENVIRONMENT, retrievedAccount.getEnvironment());
        assertEquals(REALM, retrievedAccount.getRealm());
    }

    @Test
    public void getAccountsNoHomeAccountIdNoRealm() {
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Test retrieval
        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(null, ENVIRONMENT, null);
        assertEquals(1, accounts.size());
        final AccountRecord retrievedAccount = accounts.get(0);
        assertEquals(HOME_ACCOUNT_ID, retrievedAccount.getHomeAccountId());
        assertEquals(ENVIRONMENT, retrievedAccount.getEnvironment());
        assertEquals(REALM, retrievedAccount.getRealm());
    }

    @Test
    public void getAccountsNoRealm() {
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);

        // Save the Account
        mAccountCredentialCache.saveAccount(account);

        // Test retrieval
        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(HOME_ACCOUNT_ID, ENVIRONMENT, null);
        assertEquals(1, accounts.size());
        final AccountRecord retrievedAccount = accounts.get(0);
        assertEquals(HOME_ACCOUNT_ID, retrievedAccount.getHomeAccountId());
        assertEquals(ENVIRONMENT, retrievedAccount.getEnvironment());
        assertEquals(REALM, retrievedAccount.getRealm());
    }

    @Test
    public void getAccountsWithMatchingHomeAccountIdEnvironment() {
        final AccountRecord account1 = new AccountRecord();
        account1.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account1.setUsername(USERNAME);
        account1.setAuthorityType(AUTHORITY_TYPE);
        account1.setHomeAccountId(HOME_ACCOUNT_ID);
        account1.setEnvironment(ENVIRONMENT);
        account1.setRealm(REALM);

        final AccountRecord account2 = new AccountRecord();
        account2.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account2.setUsername(USERNAME);
        account2.setAuthorityType(AUTHORITY_TYPE);
        account2.setHomeAccountId(HOME_ACCOUNT_ID);
        account2.setEnvironment(ENVIRONMENT);
        account2.setRealm(REALM2);

        final AccountRecord account3 = new AccountRecord();
        account3.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account3.setUsername(USERNAME);
        account3.setAuthorityType(AUTHORITY_TYPE);
        account3.setHomeAccountId(HOME_ACCOUNT_ID);
        account3.setEnvironment(ENVIRONMENT);
        account3.setRealm(REALM3);

        final AccountRecord account4 = new AccountRecord();
        account4.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account4.setUsername(USERNAME);
        account4.setAuthorityType(AUTHORITY_TYPE);
        account4.setHomeAccountId(HOME_ACCOUNT_ID);
        account4.setEnvironment("Foo");
        account4.setRealm(REALM);

        // Save the Accounts
        mAccountCredentialCache.saveAccount(account1);
        mAccountCredentialCache.saveAccount(account2);
        mAccountCredentialCache.saveAccount(account3);
        mAccountCredentialCache.saveAccount(account4);

        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(HOME_ACCOUNT_ID, ENVIRONMENT, null);
        assertEquals(3, accounts.size());
    }

    @Test
    public void getAccountsWithMatchingEnvironmentRealm() {
        final AccountRecord account1 = new AccountRecord();
        account1.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account1.setUsername(USERNAME);
        account1.setAuthorityType(AUTHORITY_TYPE);
        account1.setHomeAccountId("Foo");
        account1.setEnvironment(ENVIRONMENT);
        account1.setRealm(REALM);

        final AccountRecord account2 = new AccountRecord();
        account2.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account2.setUsername(USERNAME);
        account2.setAuthorityType(AUTHORITY_TYPE);
        account2.setHomeAccountId("Bar");
        account2.setEnvironment(ENVIRONMENT);
        account2.setRealm(REALM);

        final AccountRecord account3 = new AccountRecord();
        account3.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account3.setUsername(USERNAME);
        account3.setAuthorityType(AUTHORITY_TYPE);
        account3.setHomeAccountId("Baz");
        account3.setEnvironment(ENVIRONMENT);
        account3.setRealm(REALM);

        final AccountRecord account4 = new AccountRecord();
        account4.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account4.setUsername(USERNAME);
        account4.setAuthorityType(AUTHORITY_TYPE);
        account4.setHomeAccountId("qux");
        account4.setEnvironment(ENVIRONMENT);
        account4.setRealm("quz");

        // Save the Accounts
        mAccountCredentialCache.saveAccount(account1);
        mAccountCredentialCache.saveAccount(account2);
        mAccountCredentialCache.saveAccount(account3);
        mAccountCredentialCache.saveAccount(account4);

        final List<AccountRecord> accounts = mAccountCredentialCache.getAccountsFilteredBy(null, ENVIRONMENT, REALM);
        assertEquals(3, accounts.size());
    }

    @Test
    public void getCredentials() {
        // Save an Account into the cache
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        mAccountCredentialCache.saveAccount(account);

        // Save an AccessToken into the cache
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm("Foo");
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        mAccountCredentialCache.saveCredential(accessToken);

        // Save a RefreshToken into the cache
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);
        mAccountCredentialCache.saveCredential(refreshToken);

        // Verify getCredentials() returns two matching elements
        final List<Credential> credentials = mAccountCredentialCache.getCredentials();
        assertTrue(credentials.size() == 2);
    }

    @Test
    public void getCredentialsNoEnvironment() {
        final RefreshTokenRecord refreshToken1 = new RefreshTokenRecord();
        refreshToken1.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken1.setEnvironment(ENVIRONMENT);
        refreshToken1.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken1.setClientId(CLIENT_ID);
        refreshToken1.setSecret(SECRET);
        refreshToken1.setTarget(TARGET);

        final RefreshTokenRecord refreshToken2 = new RefreshTokenRecord();
        refreshToken2.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken2.setEnvironment(ENVIRONMENT_LEGACY);
        refreshToken2.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken2.setClientId(CLIENT_ID);
        refreshToken2.setSecret(SECRET);
        refreshToken2.setTarget(TARGET);

        mAccountCredentialCache.saveCredential(refreshToken1);
        mAccountCredentialCache.saveCredential(refreshToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                null, // * wildcard
                CredentialType.RefreshToken,
                CLIENT_ID,
                REALM,
                TARGET
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void getCredentialsNoCredentialType() {
        try {
            mAccountCredentialCache.getCredentialsFilteredBy(
                    HOME_ACCOUNT_ID,
                    ENVIRONMENT,
                    null,
                    CLIENT_ID,
                    REALM,
                    TARGET
            );
            fail();
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getCredentialsNoClientId() {
        try {
            mAccountCredentialCache.getCredentialsFilteredBy(
                    HOME_ACCOUNT_ID,
                    ENVIRONMENT,
                    CredentialType.RefreshToken,
                    null,
                    REALM,
                    TARGET
            );
            fail();
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getCredentialsComplete() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.RefreshToken,
                CLIENT_ID,
                REALM,
                TARGET
        );
        assertEquals(1, credentials.size());
        final Credential retrievedCredential = credentials.get(0);
        assertEquals(
                CredentialType.RefreshToken.name(),
                retrievedCredential.getCredentialType()
        );
    }

    @Test
    public void getCredentialsCaseInsensitive() {
        // Uppercase the value we're filtering on to assert
        // that the match is case insensitive
        final String searchTarget = TARGET.toUpperCase();

        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.RefreshToken,
                CLIENT_ID,
                REALM,
                searchTarget
        );
        assertEquals(1, credentials.size());
        final Credential retrievedCredential = credentials.get(0);
        assertEquals(
                CredentialType.RefreshToken.name(),
                retrievedCredential.getCredentialType()
        );
    }

    @Test
    public void getCredentialsPartialMatch() {
        final String[] targetScopes = TARGET.split("\\s+");

        // Just in case this value changes on us, just assert that it take the expected format
        assertEquals(3, targetScopes.length);

        // Let's grab a subset of these in a different order and make sure we still get the right
        // results back
        final String searchTarget = targetScopes[2] + " " + targetScopes[0];

        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.RefreshToken,
                CLIENT_ID,
                REALM,
                searchTarget
        );
        assertEquals(1, credentials.size());
        final Credential retrievedCredential = credentials.get(0);
        assertEquals(
                CredentialType.RefreshToken.name(),
                retrievedCredential.getCredentialType()
        );
    }

    @Test
    public void getCredentialsPartialMatchWithCapitalization() {
        final String[] targetScopes = TARGET.split("\\s+");

        // Just in case this value changes on us, just assert that it take the expected format
        assertEquals(3, targetScopes.length);

        // Let's grab a subset of these in a different order and make sure we still get the right
        // results back
        final String searchTarget = targetScopes[2].toUpperCase()
                + " "
                + targetScopes[0].toUpperCase();

        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.RefreshToken,
                CLIENT_ID,
                REALM,
                searchTarget
        );
        assertEquals(1, credentials.size());
        final Credential retrievedCredential = credentials.get(0);
        assertEquals(
                CredentialType.RefreshToken.name(),
                retrievedCredential.getCredentialType()
        );
    }

    @Test
    public void getCredentialsNoHomeAccountId() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);
        refreshToken.setHomeAccountId("Foo");
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId("Bar");
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                null,
                ENVIRONMENT,
                CredentialType.RefreshToken,
                CLIENT_ID,
                REALM,
                TARGET
        );
        assertEquals(1, credentials.size());
    }

    @Test
    public void getCredentialsNoHomeAccountIdNoRealm() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setSecret(SECRET);
        refreshToken.setHomeAccountId("Foo");
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId("Bar");
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        final AccessTokenRecord accessToken2 = new AccessTokenRecord();
        accessToken2.setCachedAt(CACHED_AT);
        accessToken2.setExpiresOn(EXPIRES_ON);
        accessToken2.setSecret(SECRET);
        accessToken2.setHomeAccountId("Baz");
        accessToken2.setRealm(REALM);
        accessToken2.setEnvironment(ENVIRONMENT);
        accessToken2.setCredentialType(CredentialType.AccessToken.name());
        accessToken2.setClientId(CLIENT_ID);
        accessToken2.setTarget(TARGET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);
        mAccountCredentialCache.saveCredential(accessToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                null,
                ENVIRONMENT,
                CredentialType.AccessToken,
                CLIENT_ID,
                null,
                TARGET
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void getCredentialsNoHomeAccountIdNoRealmNoTarget() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setSecret(SECRET);
        refreshToken.setHomeAccountId("Foo");
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId("Bar");
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        final AccessTokenRecord accessToken2 = new AccessTokenRecord();
        accessToken2.setCachedAt(CACHED_AT);
        accessToken2.setExpiresOn(EXPIRES_ON);
        accessToken2.setSecret(SECRET);
        accessToken2.setHomeAccountId("Baz");
        accessToken2.setRealm(REALM);
        accessToken2.setEnvironment(ENVIRONMENT);
        accessToken2.setCredentialType(CredentialType.AccessToken.name());
        accessToken2.setClientId(CLIENT_ID);
        accessToken2.setTarget("qux");

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);
        mAccountCredentialCache.saveCredential(accessToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                null,
                ENVIRONMENT,
                CredentialType.AccessToken,
                CLIENT_ID,
                null,
                null
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void getCredentialsNoTarget() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setSecret(SECRET);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        final AccessTokenRecord accessToken2 = new AccessTokenRecord();
        accessToken2.setCachedAt(CACHED_AT);
        accessToken2.setExpiresOn(EXPIRES_ON);
        accessToken2.setSecret(SECRET);
        accessToken2.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken2.setRealm(REALM);
        accessToken2.setEnvironment(ENVIRONMENT);
        accessToken2.setCredentialType(CredentialType.AccessToken.name());
        accessToken2.setClientId(CLIENT_ID);
        accessToken2.setTarget("qux");

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);
        mAccountCredentialCache.saveCredential(accessToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.AccessToken,
                CLIENT_ID,
                REALM,
                null
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void getCredentialsNoRealm() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setSecret(SECRET);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm("Foo");
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        final AccessTokenRecord accessToken2 = new AccessTokenRecord();
        accessToken2.setCachedAt(CACHED_AT);
        accessToken2.setExpiresOn(EXPIRES_ON);
        accessToken2.setSecret(SECRET);
        accessToken2.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken2.setRealm("Bar");
        accessToken2.setEnvironment(ENVIRONMENT);
        accessToken2.setCredentialType(CredentialType.AccessToken.name());
        accessToken2.setClientId(CLIENT_ID);
        accessToken2.setTarget(TARGET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);
        mAccountCredentialCache.saveCredential(accessToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.AccessToken,
                CLIENT_ID,
                null,
                TARGET
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void getCredentialsNoRealmNoTarget() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm("Foo");
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);

        final AccessTokenRecord accessToken2 = new AccessTokenRecord();
        accessToken2.setCredentialType(CredentialType.AccessToken.name());
        accessToken2.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken2.setRealm("Bar");
        accessToken2.setEnvironment(ENVIRONMENT);
        accessToken2.setClientId(CLIENT_ID);
        accessToken2.setTarget("qux");
        accessToken2.setCachedAt(CACHED_AT);
        accessToken2.setExpiresOn(EXPIRES_ON);
        accessToken2.setSecret(SECRET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);
        mAccountCredentialCache.saveCredential(accessToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                HOME_ACCOUNT_ID,
                ENVIRONMENT,
                CredentialType.AccessToken,
                CLIENT_ID,
                null,
                null
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void getCredentialsNoHomeAccountIdNoTarget() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);

        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        accessToken.setHomeAccountId("Quz");
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);

        final AccessTokenRecord accessToken2 = new AccessTokenRecord();
        accessToken2.setCachedAt(CACHED_AT);
        accessToken2.setExpiresOn(EXPIRES_ON);
        accessToken2.setSecret(SECRET);
        accessToken2.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken2.setRealm(REALM);
        accessToken2.setEnvironment(ENVIRONMENT);
        accessToken2.setCredentialType(CredentialType.AccessToken.name());
        accessToken2.setClientId(CLIENT_ID);
        accessToken2.setTarget(TARGET);

        // Save the Credentials
        mAccountCredentialCache.saveCredential(refreshToken);
        mAccountCredentialCache.saveCredential(accessToken);
        mAccountCredentialCache.saveCredential(accessToken2);

        List<Credential> credentials = mAccountCredentialCache.getCredentialsFilteredBy(
                null,
                ENVIRONMENT,
                CredentialType.AccessToken,
                CLIENT_ID,
                REALM,
                null
        );
        assertEquals(2, credentials.size());
    }

    @Test
    public void clearAccounts() {
        // Save an Account into the cache
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        mAccountCredentialCache.saveAccount(account);

        // Save an AccessToken into the cache
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm(REALM);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        mAccountCredentialCache.saveCredential(accessToken);

        // Save a RefreshToken into the cache
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);
        mAccountCredentialCache.saveCredential(refreshToken);

        // Call clearAccounts()
        mAccountCredentialCache.removeAccount(account);

        // Verify getAccounts() returns zero items
        assertTrue(mAccountCredentialCache.getAccounts().isEmpty());

        // Verify getCredentials() returns two items
        final List<Credential> credentials = mAccountCredentialCache.getCredentials();
        assertTrue(credentials.size() == 2);
    }

    @Test
    public void clearCredentials() {
        // Save an Account into the cache
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        mAccountCredentialCache.saveAccount(account);

        // Save an AccessToken into the cache
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setRealm(REALM);
        accessToken.setTarget(TARGET);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);
        accessToken.setSecret(SECRET);
        mAccountCredentialCache.saveCredential(accessToken);

        // Save a RefreshToken into the cache
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);
        mAccountCredentialCache.saveCredential(refreshToken);

        // Call clearCredentials()
        mAccountCredentialCache.removeCredential(accessToken);
        mAccountCredentialCache.removeCredential(refreshToken);

        // Verify getAccounts() returns 1 item
        assertEquals(1, mAccountCredentialCache.getAccounts().size());

        // Verify getCredentials() returns zero items
        assertEquals(0, mAccountCredentialCache.getCredentials().size());
    }

    @Test
    public void clearAll() {
        // Save an Account into the cache
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);
        account.setLocalAccountId(LOCAL_ACCOUNT_ID);
        account.setUsername(USERNAME);
        account.setAuthorityType(AUTHORITY_TYPE);
        mAccountCredentialCache.saveAccount(account);

        // Save an AccessToken into the cache
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setRealm("Foo");
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setClientId(CLIENT_ID);
        accessToken.setTarget(TARGET);
        accessToken.setCachedAt(CACHED_AT);
        accessToken.setExpiresOn(EXPIRES_ON);
        accessToken.setSecret(SECRET);
        mAccountCredentialCache.saveCredential(accessToken);

        // Save a RefreshToken into the cache
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setCredentialType(CredentialType.RefreshToken.name());
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setClientId(CLIENT_ID);
        refreshToken.setSecret(SECRET);
        refreshToken.setTarget(TARGET);
        mAccountCredentialCache.saveCredential(refreshToken);

        // Call clearAll()
        mAccountCredentialCache.clearAll();

        // Verify getAccounts() returns zero items
        assertTrue(mAccountCredentialCache.getAccounts().isEmpty());

        // Verify getCredentials() returns zero items
        assertTrue(mAccountCredentialCache.getCredentials().isEmpty());
    }

    @Test(expected = RuntimeException.class) // TODO Should this *really* throw a RuntimeException
    public void testThrowsExceptionForMalformedCredentialCacheKey() {
        mAccountCredentialCache.getCredential("Malformed cache key");
    }

    @Test
    public void noValueForCacheKeyAccount() {
        assertEquals(0, mAccountCredentialCache.getAccounts().size());
        final AccountRecord account = (AccountRecord) mAccountCredentialCache.getAccount("No account");
        assertNull(account);
    }

    @Test
    public void noValueForCacheKeyAccessToken() {
        assertEquals(0, mAccountCredentialCache.getCredentials().size());
        final AccessTokenRecord accessToken = (AccessTokenRecord) mAccountCredentialCache.getCredential(CACHE_VALUE_SEPARATOR + CredentialType.AccessToken.name().toLowerCase() + CACHE_VALUE_SEPARATOR);
        assertNull(accessToken);
    }

    @Test
    public void noValueForCacheKeyRefreshToken() {
        assertEquals(0, mAccountCredentialCache.getCredentials().size());
        final RefreshTokenRecord refreshToken = (RefreshTokenRecord) mAccountCredentialCache.getCredential(CACHE_VALUE_SEPARATOR + CredentialType.RefreshToken.name().toLowerCase() + CACHE_VALUE_SEPARATOR);
        assertNull(refreshToken);
    }

    @Test
    public void noValueForCacheKeyIdToken() {
        assertEquals(0, mAccountCredentialCache.getCredentials().size());
        final IdTokenRecord idToken = (IdTokenRecord) mAccountCredentialCache.getCredential(CACHE_VALUE_SEPARATOR + CredentialType.IdToken.name().toLowerCase() + CACHE_VALUE_SEPARATOR);
        assertNull(idToken);
    }

    @Test
    public void malformedJsonCacheValueForAccount() {
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(account);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" \"not an account\"}");

        final AccountRecord malformedAccount = mAccountCredentialCache.getAccount(cacheKey);
        assertNull(malformedAccount);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedCacheValueForAccount() {
        final AccountRecord account = new AccountRecord();
        account.setHomeAccountId(HOME_ACCOUNT_ID);
        account.setEnvironment(ENVIRONMENT);
        account.setRealm(REALM);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(account);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" : \"not an account\"}");

        final AccountRecord malformedAccount = mAccountCredentialCache.getAccount(cacheKey);
        assertNull(malformedAccount);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedJsonCacheValueForAccessToken() {
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(accessToken);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" \"not an accessToken\"}");

        final AccessTokenRecord malformedAccessToken = (AccessTokenRecord) mAccountCredentialCache.getCredential(cacheKey);
        assertNull(malformedAccessToken);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedCacheValueForAccessToken() {
        final AccessTokenRecord accessToken = new AccessTokenRecord();
        accessToken.setHomeAccountId(HOME_ACCOUNT_ID);
        accessToken.setEnvironment(ENVIRONMENT);
        accessToken.setCredentialType(CredentialType.AccessToken.name());
        accessToken.setClientId(CLIENT_ID);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(accessToken);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" : \"not an accessToken\"}");

        final AccessTokenRecord malformedAccessToken = (AccessTokenRecord) mAccountCredentialCache.getCredential(cacheKey);
        assertNull(malformedAccessToken);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedJsonCacheValueForRefreshToken() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.AccessToken.name());
        refreshToken.setClientId(CLIENT_ID);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(refreshToken);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" \"not a refreshToken\"}");

        final RefreshTokenRecord malformedRefreshToken = (RefreshTokenRecord) mAccountCredentialCache.getCredential(cacheKey);
        assertNull(malformedRefreshToken);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedCacheValueForRefreshToken() {
        final RefreshTokenRecord refreshToken = new RefreshTokenRecord();
        refreshToken.setHomeAccountId(HOME_ACCOUNT_ID);
        refreshToken.setEnvironment(ENVIRONMENT);
        refreshToken.setCredentialType(CredentialType.AccessToken.name());
        refreshToken.setClientId(CLIENT_ID);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(refreshToken);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" : \"not a refreshToken\"}");

        final RefreshTokenRecord malformedRefreshToken = (RefreshTokenRecord) mAccountCredentialCache.getCredential(cacheKey);
        assertNull(malformedRefreshToken);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedJsonCacheValueForIdToken() {
        final IdTokenRecord idToken = new IdTokenRecord();
        idToken.setHomeAccountId(HOME_ACCOUNT_ID);
        idToken.setEnvironment(ENVIRONMENT);
        idToken.setCredentialType(CredentialType.IdToken.name());
        idToken.setClientId(CLIENT_ID);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(idToken);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\"  \"not an idToken\"}");

        final IdTokenRecord restoredIdToken = (IdTokenRecord) mAccountCredentialCache.getCredential(cacheKey);
        assertNull(restoredIdToken);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    @Test
    public void malformedCacheValueForIdToken() {
        final IdTokenRecord idToken = new IdTokenRecord();
        idToken.setHomeAccountId(HOME_ACCOUNT_ID);
        idToken.setEnvironment(ENVIRONMENT);
        idToken.setCredentialType(CredentialType.IdToken.name());
        idToken.setClientId(CLIENT_ID);

        // Generate a cache key
        final String cacheKey = mDelegate.generateCacheKey(idToken);

        mSharedPreferencesFileManager.putString(cacheKey, "{\"thing\" : \"not an idToken\"}");

        final IdTokenRecord restoredIdToken = (IdTokenRecord) mAccountCredentialCache.getCredential(cacheKey);
        assertNull(restoredIdToken);
        assertNull(mSharedPreferencesFileManager.getString(cacheKey));
    }

    public void persistAndRestoreExtraClaimsAccessToken() {
        // TODO
    }

    public void persistAndRestoreExtraClaimsRefreshToken() {
        // TODO
    }

    public void persistAndRestoreExtraClaimsIdToken() {
        // TODO
    }

}
