package org.tightblog.dto;

import java.time.Instant;

public record UserDto(String id, String screenName, String userName, String emailAddress,
                      String userStatus, String globalRole, Instant dateCreated, Instant lastLogin,
                      String registrationActivationCode,
                      String passwordText, String passwordConfirm, boolean hasMfaSecret, boolean eraseMfaSecret) {

    // for sending to front-end, no password info provided
    public UserDto(String id, String screenName, String userName, String emailAddress,
                   String userStatus, String globalRole, Instant dateCreated, Instant lastLogin,
                   String registrationActivationCode) {
        this(id, screenName, userName, emailAddress,
                userStatus, globalRole, dateCreated, lastLogin,
                registrationActivationCode,
                null, null, false, false);
    }
}
