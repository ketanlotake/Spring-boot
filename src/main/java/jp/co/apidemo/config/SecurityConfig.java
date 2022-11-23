package jp.co.apidemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jp.co.apidemo.filter.CustomAuthenticationFilter;
import jp.co.apidemo.filter.CustomAuthorizationFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.security.config.http.SessionCreationPolicy.*;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private  UserDetailsService userDetailsService;
    private  BCryptPasswordEncoder bCryptPasswordEncoder;

    public static final String[] PUBLIC_URLS={
     
       "/v2/api-docs",
        "/h2-console/**",
        "/swagger-ui.html**",
        "/webjars/**",
        "/swagger-resources/**"
    };

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.bCryptPasswordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    /*  
        Role             | SAVE EMPLOYEE/ROLE | RETRIVE |DELETE | UPDATE | 
    | -------------      | ------------------ | --------| ----- |--------|
    | ROLE_MANAGER       | YES                | YES     | YES   | YES    |
    | ROLE_TEAM_LEADER   | NO                 | YES     | NO    | NO     |
    | ROLE_PROJECT_LEADER| NO                 | YES     | NO    | NO     |
    | ROLE_ENGINEER      | NO                 | YES     | NO    | NO     | */
        
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManagerBean());
        customAuthenticationFilter.setFilterProcessesUrl("/api/v1/login");
        http.authorizeRequests().antMatchers("/").permitAll();
        http.authorizeRequests().antMatchers(PUBLIC_URLS).permitAll();
        http.csrf().disable();
        http.headers().frameOptions().disable();
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        //http.authorizeRequests().antMatchers("/api/v1/h2-console/**").permitAll();
        http.authorizeRequests().antMatchers("/api/v1/login**", "/api/v1/token/refresh/**").permitAll();
        http.authorizeRequests().antMatchers(GET, "/api/v1/employee/get**").permitAll();
        http.authorizeRequests().antMatchers(POST, "/api/v1/employee/save**").hasAnyAuthority("ROLE_MANAGER");
        http.authorizeRequests().antMatchers(POST, "/api/v1/role/save**").hasAnyAuthority("ROLE_MANAGER");
        http.authorizeRequests().antMatchers(POST, "/api/v1/role/addtoemployee**").hasAnyAuthority("ROLE_MANAGER");
        http.authorizeRequests().antMatchers(DELETE, "/api/v1/employee/delete**").hasAnyAuthority("ROLE_MANAGER");
        http.authorizeRequests().antMatchers(PUT, "/api/v1/employee/update**").hasAnyAuthority("ROLE_TEAM_LEADER");
        http.authorizeRequests().anyRequest().authenticated();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    } 
}
