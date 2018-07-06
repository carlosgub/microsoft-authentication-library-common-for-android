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
package com.microsoft.identity.common.exception;

public final class ErrorStrings {

    private ErrorStrings() {
        // Utility class.
    }

    /**
     * There are multiple cache entries found, the sdk cannot pick the correct access token
     * or refresh token from the cache. Likely it's a bug in the sdk when caching tokens or authority
     * is not proviced in the silent request and multiple tokens were found.
     */
    public static final String MULTIPLE_MATCHING_TOKENS_DETECTED = "multiple_matching_tokens_detected";

    /**
     * No active network is available on the device.
     */
    public static final String DEVICE_NETWORK_NOT_AVAILABLE = "device_network_not_available";

    /**
     * Network is available but device is in the doze mode.
     */
    public static final String NO_NETWORK_CONNECTION_POWER_OPTIMIZATION = "device_network_not_available_doze_mode";

    /**
     * The sdk failed to parse the Json format.
     */
    public static final String JSON_PARSE_FAILURE = "json_parse_failure";

    /**
     * IOException happened, could be the device/network errors.
     */
    public static final String IO_ERROR = "io_error";

    /**
     * The url is malformed.  Likely caused when constructing the auth request, authority, or redirect URI.
     */
    public static final String MALFORMED_URL = "malformed_url";

    /**
     * The encoding is not supported by the device.
     */
    public static final String UNSUPPORTED_ENCODING = "unsupported_encoding";

    /**
     * The algorithm used to generate pkce challenge is not supported.
     */
    public static final String NO_SUCH_ALGORITHM = "no_such_algorithm";

    /**
     * JWT returned by the server is not valid, empty or malformed.
     */
    public static final String INVALID_JWT = "invalid_jwt";

    /**
     * State from authorization response did not match the state in the authorization request.
     * For authorization requests, the sdk will verify the state returned from redirect and the one sent in the request.
     */
    public static final String STATE_MISMATCH = "state_mismatch";

    /**
     * The intent to launch Activity is not resolvable by the OS or the intent doesn't contain the required data.
     */
    public static final String UNRESOLVABLE_INTENT = "unresolvable_intent";

    /**
     * Unsupported url, cannot perform adfs authority validation.
     */
    public static final String UNSUPPORTED_URL = "unsupported_url";

    /**
     * The authority is not supported for authority validation. The sdk supports b2c authority, but we don't support b2c authority validation yet.
     * Only well-known host will be supported.
     */
    public static final String AUTHORITY_VALIDATION_NOT_SUPPORTED = "authority_validation_not_supported";

    /**
     * chrome_not_installed: Chrome is not installed on the device. The sdk uses chrome custom tab for
     * authorization requests if available, and will fall back to chrome browser.
     */
    public static final String CHROME_NOT_INSTALLED = "chrome_not_installed";

    /**
     * The user provided in the acquire token request doesn't match the user returned from server.
     */
    public static final String USER_MISMATCH = "user_mismatch";

    /**
     * Extra query parameters set by the client app is already sent by the sdk.
     */
    public static final String DUPLICATE_QUERY_PARAMETER = "duplicate_query_parameter";

    /**
     * Temporary non-exposed error code to indicate that ADFS authority validation fails. ADFS as authority is not supported
     * for preview.
     */
    static final String ADFS_AUTHORITY_VALIDATION_FAILED = "adfs_authority_validation_failed";

    /**
     * Failed to unwrap with the android keystore.
     */
    public static final String ANDROIDKEYSTORE_FAILED = "android_keystore_failed";

    /**
     * The authority url is invalid.
     */
    public static final String AUTHORITY_URL_NOT_VALID = "authority_url_not_valid";

    /**
     * Encounter errors during encryption.
     */
    public static final String ENCRYPTION_ERROR = "encryption_error";

    /**
     * Encounter errors during decryption.
     */
    public static final String DECRYPTION_ERROR = "decryption_error";

    /**
     * This request is missing a required parameter, includes an invalid parameter, includes a parameter more than
     * once, or is otherwise malformed.
     */
    public static final String INVALID_REQUEST = "invalid_request";

    /**
     * The client is not authorized to request an authorization code.
     */
    public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";

    /**
     * The resource owner or authorization server denied the request.
     */
    public static final String ACCESS_DENIED = "access_denied";

    /**
     * The request scope is invalid, unknown or malformed.
     */
    public static final String INVALID_SCOPE = "invalid_scope";

    /**
     * Represents 500/503/504 error codes.
     */
    public static final String SERVICE_NOT_AVAILABLE = "service_not_available";

    /**
     * Represents {@link java.net.SocketTimeoutException}.
     */
    public static final String REQUEST_TIMEOUT = "request_timeout";

    /**
     * Authority validation failed.
     */
    public static final String INVALID_INSTANCE = "invalid_instance";

    /**
     * Request to server failed, but no error and error_description is returned back from the service.
     */
    public static final String UNKNOWN_ERROR = "unknown_error";

    /**
     * Account is missing schema-required fields.
     */
    public static final String ACCOUNT_IS_SCHEMA_NONCOMPLIANT = "Account is missing schema-required fields.";

    /**
     * Credential is missing schema-required fields.
     */
    public static final String CREDENTIAL_IS_SCHEMA_NONCOMPLIANT = "Credential is missing schema-required fields.";

    /**
     * Device certificate request is invalid.
     */
    public static final String DEVICE_CERTIFICATE_REQUEST_INVALID = "Device certificate request is invalid";

    /**
     * Certificate encoding is not generated.
     */
    public static final String CERTIFICATE_ENCODING_ERROR = "Certificate encoding is not generated";

    /**
     * Key Chain private key exception.
     */
    public static final String KEY_CHAIN_PRIVATE_KEY_EXCEPTION = "Key Chain private key exception";

    /**
     * Signature exception.
     */
    public static final String SIGNATURE_EXCEPTION = "Signature exception";

    /**
     * Device certificate API has exception.
     */
    public static final String DEVICE_CERTIFICATE_API_EXCEPTION = "Device certificate API has exception";

    /**
     * The redirectUri for broker is invalid.
     */
    public static final String DEVELOPER_REDIRECTURI_INVALID = "The redirectUri for broker is invalid";

    /**
     * WebView  redirect url is not SSL protected.
     */
    public static final String WEBVIEW_REDIRECTURL_NOT_SSL_PROTECTED = "The webView was redirected to an unsafe URL";
}
