package org.tightblog.ui.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.tightblog.business.WebloggerStaticConfig;
import org.tightblog.business.WebloggerStaticConfig.MFAOption;
//import org.tightblog.pojos.User;
//import org.tightblog.util.I18nMessages;

//import java.util.Locale;

public class MultiFactorAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        // check username & password first
        Authentication result = super.authenticate(auth);

        // if here, username & password were correct, so check validation code if we're using MFA
        if (MFAOption.REQUIRED.equals(WebloggerStaticConfig.getMFAOption())) {
            String verificationCode = ((CustomWebAuthenticationDetails) auth.getDetails()).getVerificationCode();

            // Check user permissions
            /*
            User user = userManager.getEnabledUserByUserName(p.getName());
            I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();
            */

            // do validation
            if (!"895".equals(verificationCode)) {
/*                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "Bad credentials")); */
                throw new InvalidVerificationCodeException("Authenticator code is invalid, please recheck the latest code" +
                        " being provided by your smartphone and try again.");
            }
        }

        return result;
    }

    public static class InvalidVerificationCodeException extends BadCredentialsException {

        public InvalidVerificationCodeException(String msg) {
            super(msg);
        }
    }

}
