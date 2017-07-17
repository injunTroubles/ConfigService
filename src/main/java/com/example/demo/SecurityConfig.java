package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by rvann on 7/14/17.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin").password("password").authorities("ADMIN", "ACTUATOR");
        auth.inMemoryAuthentication()
                .withUser("client").password("password").authorities("CONFIG_CLIENT");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling()
                .and()
                .authorizeRequests()
                .antMatchers("/version/**").permitAll()
                .antMatchers("/actuator/health/**").permitAll()
                .antMatchers("/actuator/info/**").permitAll()
                .antMatchers("/actuator/**", "/decrypt/**").hasAuthority("ADMIN")
                .antMatchers("/**").hasAnyAuthority("CONFIG_CLIENT", "ADMIN")
                .and().httpBasic();
    }
}
