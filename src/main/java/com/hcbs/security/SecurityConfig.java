package com.hcbs.security;

//import com.hcbs.views.LoginView;
//import com.vaadin.flow.spring.security.VaadinWebSecurity;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.security.core.authority.AuthorityUtils;
//import java.util.Set;
//
//@EnableWebSecurity
//@Configuration
//public class SecurityConfig extends VaadinWebSecurity {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // 1. Configure custom permitAll rules BEFORE super.configure(http).
//        // DO NOT add .anyRequest() here because super.configure(http) handles it internally for all Vaadin routes!
//        http.authorizeHttpRequests(auth -> auth
//                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
//                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
//        );
//
//        // 2. Allow H2 console frames
//        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
//
//        // 3. Disable CSRF for H2 console
//        http.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")));
//
//        // 4. Let Vaadin set up its internal routing rules and static resources permissions!
//        super.configure(http);
//
//        // 5. Set up the login view. This automatically allows anonymous access to the login view and its static resources.
//        setLoginView(http, "/login");
//
//        // 6. Custom Success Handler for role-based redirection
//        http.formLogin(form -> form.successHandler((request, response, authentication) -> {
//            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
//            if (roles.contains("ROLE_ADMIN")) {
//                response.sendRedirect("/admin");
//            } else if (roles.contains("ROLE_STAFF")) {
//                response.sendRedirect("/staff");
//            } else if (roles.contains("ROLE_MANAGER")) {
//                response.sendRedirect("/manager");
//            } else {
//                response.sendRedirect("/");
//            }
//        }));
//    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}


import com.hcbs.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.authority.AuthorityUtils;
import java.util.Set;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(new AntPathRequestMatcher("/public/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login")).permitAll() // <--- 添加这一行
        );

        // Allow H2 console frames
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        // Disable CSRF for H2 console
        // 注意：通常建议对登录接口也禁用CSRF或者确保Thymeleaf等模板引擎正确携带CSRF Token
        // 如果登录提交的是POST请求且启用了CSRF，需要确保前端表单包含 _csrf token
        http.csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")));

        super.configure(http);
        setLoginView(http, "/login");

        http.formLogin(form -> form.successHandler((request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin");
            } else if (roles.contains("ROLE_STAFF")) {
                response.sendRedirect("/staff");
            } else if (roles.contains("ROLE_MANAGER")) {
                response.sendRedirect("/manager");
            } else {
                response.sendRedirect("/");
            }
        }));
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}