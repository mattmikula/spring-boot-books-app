package com.matt.books.services;


import com.matt.books.models.Account;
import com.matt.books.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountDetailsService implements UserDetailsService {

    @Autowired
    AccountRepository accounts;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Account> account = accounts.findByUsername(username);
        if (!account.isPresent()){
            throw new UsernameNotFoundException(username + " was not found");
        }
        return new org.springframework.security.core.userdetails.User(
                account.get().getUsername(),
                account.get().getPassword(),
                AuthorityUtils.createAuthorityList(account.get().getRoles())
        );
    }
}
