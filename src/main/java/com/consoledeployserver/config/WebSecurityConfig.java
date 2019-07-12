package com.consoledeployserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * spring security配置
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable()   // 关闭防跨域攻击
        http.csrf().requireCsrfProtectionMatcher(new CsrfSecurityRequestMatcher());
        http
                .authorizeRequests()
                .antMatchers("/").hasRole("USER")
                .and()
                .formLogin().loginPage("/login")
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/login")
        ;
    }

    /**
     * Spring security 5.0中新增了多种加密方式，也改变了密码的格式。会报错:There is no PasswordEncoder mapped for the id "null"
     * 添加用户root/123
     * @return
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
//                .withUser("root").password("123").roles("USER");
                .passwordEncoder(new BCryptPasswordEncoder())  // 设置加密方式
                .withUser("root")
                .password(new BCryptPasswordEncoder().encode("123"))
                .roles("USER");
    }
}
