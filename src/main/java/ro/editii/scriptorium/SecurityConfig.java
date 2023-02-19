package ro.editii.scriptorium;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User
                .withUsername("petru")
                .password(passwordEncoder().encode("xilofon"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("petru")
//                .password(passwordEncoder().encode(""))
//                .authorities("ROLE_ADMIN");
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors()
            .and()
                .authorizeRequests().antMatchers("/admin/**").authenticated()
            .and()
                .httpBasic();

        return http.build();
    }
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .cors()
//            .and()
//                .authorizeRequests().antMatchers("/admin/").authenticated()
//            .and()
//                .httpBasic();
//
////        http.addFilterAfter(new CustomFilter(),
////                BasicAuthenticationFilter.class);
//    }
}
