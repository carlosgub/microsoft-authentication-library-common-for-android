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
package com.microsoft.identity.common.internal.providers.microsoft.activedirectoryfederationservices;

import com.microsoft.identity.common.BaseAccount;

import java.util.List;

/**
 * The Active Directory Federation Services 2012 R2 Account Object
 * NOTE: Since ADFS 2012 R2 does not support OIDC there is no id token
 * It's unclear to me how ADFS 2012 Accounts should be identified... I believe there is a
 * need for the API to support generating a unique identifier to a user and returning that to the caller
 * OR for the caller to provide a unique identifier prior to initiating the request
 */
@SuppressWarnings("PMD") // Suppressing PMD warning for multiple usages of the String "Method stub!"
public class ActiveDirectoryFederationServices2012R2Account extends BaseAccount {

    @Override
    public String getUniqueIdentifier() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public List<String> getCacheIdentifiers() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getHomeAccountId() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getEnvironment() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getRealm() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getLocalAccountId() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getUsername() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getAuthorityType() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getAlternativeAccountId() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getFirstName() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getFamilyName() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getMiddleName() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Method stub!");
    }

    @Override
    public String getAvatarUrl() {
        throw new UnsupportedOperationException("Method stub!");
    }
}
