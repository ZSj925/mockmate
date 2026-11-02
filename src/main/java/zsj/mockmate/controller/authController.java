package zsj.mockmate.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zsj.mockmate.pojo.Result;
import zsj.mockmate.pojo.dto.LoginRequest;
import zsj.mockmate.pojo.vo.LoginResponse;
import zsj.mockmate.service.authService;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private authService authService;

    public AuthController(authService authService){
        this.authService = authService;
    }

    /**
     * 登录
     * @param login
     * @return
     */
    @PostMapping("/doLogin")
    public Result<LoginResponse> doLogin(@RequestBody LoginRequest login){
        return Result.success(authService.doLogin(login.username(),login.password()));
    }

    /**
     * 退出登录：注销当前 token，并清掉 Sa-Token 写的 cookie。
     * 该路径已在 SaTokenConfigure 里放行，所以 token 已过期时也能调用，前端可以放心地无条件调一次。
     */
    @PostMapping("/doLogout")
    public Result<Void> doLogout(){
        Object loginId = StpUtil.getLoginIdDefaultNull();
        StpUtil.logout();
        log.info("用户退出登录: {}", loginId);
        return Result.success();
    }

}
