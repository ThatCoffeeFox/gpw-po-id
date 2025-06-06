package pl.gpwpoid.origin.utils;

import org.springframework.security.core.userdetails.UserDetails;

public interface ExtendedUserDetails extends UserDetails {

    Integer getAccountId();
}
