package org.librairy.service.learner.controllers;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import java.util.StringTokenizer;

/**
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("#{environment['LIBRAIRY_API_USERS']?:'${librairy.api.users}'}")
    String users;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().fullyAuthenticated();
        http.httpBasic();
        http.csrf().disable();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        UserDetailsManagerConfigurer.UserDetailsBuilder userDetails = auth.inMemoryAuthentication()
                .withUser("librairy").password("l1brA1ry").roles("ADMIN");


        StringTokenizer tokenizer = new StringTokenizer(users,";");

        while(tokenizer.hasMoreTokens()){
            String user = tokenizer.nextToken();
            String name = StringUtils.substringBefore(user,":");
            String pwd = StringUtils.substringAfter(user,":");
            userDetails = userDetails.and().withUser(name).password(pwd).roles("USER");
            LOG.info("Added user: " + user);

        }
    }



}
