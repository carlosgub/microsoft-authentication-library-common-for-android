package com.microsoft.identity.common.internal.util;

import android.support.annotation.Nullable;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.microsoft.identity.common.exception.ErrorStrings;
import com.microsoft.identity.common.exception.ServiceException;

import java.io.UnsupportedEncodingException;

/**
 * String utilities
 */
public class StringUtil {
    public static boolean isEmpty(final String message) {
        return message == null || message.trim().length() == 0;
    }

    /**
     * This method is to check if the specified Json could be deserialized into an object of the specified class
     * @param jsonInString
     * @param cls
     * @return true if the jsonInString could be deserialized into the specified class. False otherwise.
     */
    public static void validateJsonFormat(final String jsonInString, @Nullable Class<?> cls) throws ServiceException {
        try {
            final Gson gson = new Gson();
            if (null == cls) {
                gson.fromJson(jsonInString, Object.class);
            } else {
                gson.fromJson(jsonInString, cls);
            }
        } catch (final JsonSyntaxException jsonSyntaxException) {
            throw new ServiceException(ErrorStrings.JSON_PARSE_FAILURE,
                    "The passed-in string is not JSON valid format.", jsonSyntaxException);
        }
    }

    public static void validateJWTFormat(final String rawJWTStr) throws ServiceException {
        final String idbody = extractJWTBody(rawJWTStr);
        final byte[] data = Base64.decode(idbody, Base64.URL_SAFE);
        try {
            final String decodedBody = new String(data, "UTF-8");
            validateJsonFormat(decodedBody, null);
        } catch (final UnsupportedEncodingException exception) {
            throw new ServiceException(ErrorStrings.UNSUPPORTED_ENCODING, exception.getMessage(), exception);
        }
    }

    static String extractJWTBody(final String idToken) throws ServiceException {
        final int firstDot = idToken.indexOf('.');
        final int secondDot = idToken.indexOf('.', firstDot + 1);
        final int invalidDot = idToken.indexOf('.', secondDot + 1);

        if (invalidDot == -1 && firstDot > 0 && secondDot > 0) {
            return idToken.substring(firstDot + 1, secondDot);
        } else {
            throw new ServiceException(ErrorStrings.INVALID_JWT, "Cannot parse IdToken", null);
        }
    }
}
