package com.microsoft.identity.common.model;

import com.google.gson.annotations.SerializedName;

/**
 * Accounts collect user displayable information about the user in each tenant (AAD) or environment
 * (MSA). Accounts also have fields necessary to lookup credentials.
 * <p>
 * Account schema only needs to be present, if there's a user available. For scenarios, where
 * there's no user present (e.g. client credential grant), only credential schema is necessary.
 */
public class Account {

    ///////////////
    // Required fields
    ///////////////

    /**
     * Unique user identifier for a given authentication scheme.
     */
    @SerializedName("unique_id")
    private String mUniqueId;

    /**
     * Entity who issued the token represented as the host portion of a URL. For AAD it's host part
     * from the authority url with an optional port. For ADFS, it's the host part of the ADFS server
     * URL.
     */
    @SerializedName("environment")
    private String mEnvironment;

    /**
     * Full tenant or organizational identifier that account belongs to. Can be null.
     */
    @SerializedName("realm")
    private String mRealm;

    /**
     * Original authority specific account identifier. Can be needed for legacy purposes. OID for
     * AAD (in some unique cases subject instead of OID) and CID for MSA.
     */
    @SerializedName("authority_account_id")
    private String mAuthorityAccountId;

    /**
     * The primary username that represents the user (corresponds to the preferred_username claim
     * in the v2.0 endpoint). It could be an email address, phone number, or a generic username
     * without a specified format. Its value is mutable and might change over time. For MSA it's
     * email. For NTLM, NTLM username.
     */
    @SerializedName("username")
    private String mUsername;

    /**
     * Account’s authority type as string (ex: AAD, MSA, MSSTS, Other).
     * Set of account types is extensible.
     */
    @SerializedName("authority_type")
    private String mAuthorityType;

    ///////////////
    // Optional Fields
    ///////////////

    /**
     * Internal representation for guest users to the tenants. Corresponds to the “altsecid” claim
     * in the id_token for AAD.
     */
    @SerializedName("guest_id")
    private String mGuestId;

    /**
     * First name for this Account.
     */
    @SerializedName("first_name")
    private String mFirstName;

    /**
     * Last name for this Account.
     */
    @SerializedName("last_name")
    private String mLastName;

    /**
     * URL corresponding to a picture for this Account.
     */
    @SerializedName("avatar_url")
    private String mAvatarUrl;

    ///////////////
    // Accessor Methods
    ///////////////

    /**
     * Gets the unique_id.
     *
     * @return The unique_id to get.
     */
    public String getUniqueId() {
        return mUniqueId;
    }

    /**
     * Sets the unique_id.
     *
     * @param uniqueId The unique_id to get.
     */
    public void setUniqueId(final String uniqueId) {
        mUniqueId = uniqueId;
    }

    /**
     * Gets the environment.
     *
     * @return The environment to get.
     */
    public String getEnvironment() {
        return mEnvironment;
    }

    /**
     * Sets the environment.
     *
     * @param environment The environment to set.
     */
    public void setEnvironment(final String environment) {
        mEnvironment = environment;
    }

    /**
     * Gets the realm.
     *
     * @return The realm to get.
     */
    public String getRealm() {
        return mRealm;
    }

    /**
     * Sets the realm.
     *
     * @param realm The realm to set.
     */
    public void setRealm(final String realm) {
        mRealm = realm;
    }

    /**
     * Gets the authority_account_id.
     *
     * @return The authority_account_id to get.
     */
    public String getAuthorityAccountId() {
        return mAuthorityAccountId;
    }

    /**
     * Sets the authority_account_id.
     *
     * @param authorityAccountId The authority_account_id to set.
     */
    public void setAuthorityAccountId(final String authorityAccountId) {
        mAuthorityAccountId = authorityAccountId;
    }

    /**
     * Gets the username.
     *
     * @return The username to get.
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Sets the username.
     *
     * @param username The username to set.
     */
    public void setUsername(final String username) {
        mUsername = username;
    }

    /**
     * Gets the authority_type.
     *
     * @return The authority_type to get.
     */
    public String getAuthorityType() {
        return mAuthorityType;
    }

    /**
     * Sets the authority_type.
     *
     * @param authorityType The authority_type to set.
     */
    public void setAuthorityType(final String authorityType) {
        mAuthorityType = authorityType;
    }

    /**
     * Gets the guest_id.
     *
     * @return The guest_id to get.
     */
    public String getGuestId() {
        return mGuestId;
    }

    /**
     * Sets the guest_id.
     *
     * @param guestId The guest_id to set.
     */
    public void setGuestId(final String guestId) {
        mGuestId = guestId;
    }

    /**
     * Gets the first_name;
     *
     * @return The first_name to get.
     */
    public String getFirstName() {
        return mFirstName;
    }

    /**
     * Sets the first_name;
     *
     * @param firstName The first_name to set.
     */
    public void setFirstName(final String firstName) {
        mFirstName = firstName;
    }

    /**
     * Gets the last_name.
     *
     * @return The last_name to get.
     */
    public String getLastName() {
        return mLastName;
    }

    /**
     * Sets the last_name.
     *
     * @param lastName The last_name to set.
     */
    public void setLastName(final String lastName) {
        mLastName = lastName;
    }

    /**
     * Gets the avatar_url.
     *
     * @return The avatar_url to get.
     */
    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    /**
     * Sets the avatar_url.
     *
     * @param avatarUrl The avatar_url to set.
     */
    public void setAvatarUrl(final String avatarUrl) {
        mAvatarUrl = avatarUrl;
    }
}