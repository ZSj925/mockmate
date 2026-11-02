package zsj.mockmate.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 Sa-Token 拦截器
 */

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    /** 前端静态页面所在目录：src/main/resources/static */
    private static final String[] STATIC_RESOURCES = {
            "/",                // 首页 → static/index.html
            "/*.html",          // login.html 等静态页
            "/css/**",
            "/js/**",
            "/img/**",
            "/favicon.ico",
            "/error"            // Spring Boot 的错误转发，不拦截
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        //
        // 注意：拦截器默认对所有请求生效，静态页面/样式/脚本也在其中。
        // 如果不过滤，浏览器请求 login.html 时会先被 checkLogin() 拦下抛 NotLoginException，
        // 于是页面永远加载不出来 —— 登录页本身就变成了"必须先登录"的死循环。
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/doLogin",   // 登录本身不拦截
                        "/auth/doLogout"   // 退出也不拦截：token 已过期时也要能走通，否则前端跳不回登录页
                )
                // 静态资源单独放行，和上面的业务接口排除项分开维护
                .excludePathPatterns(STATIC_RESOURCES);
    }

}
