package zsj.mockmate.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;
import zsj.mockmate.mapper.authMapper;
import zsj.mockmate.pojo.ApiException;
import zsj.mockmate.pojo.entity.UserEntity;
import zsj.mockmate.pojo.vo.LoginResponse;
import zsj.mockmate.service.authService;

@Service
public class authServiceImpl implements authService {

    private authMapper authMapper;

    public authServiceImpl(authMapper authMapper) {
        this.authMapper = authMapper;
    }

    /**
     * 登录的业务层接口
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public LoginResponse doLogin(String username, String password) {

        //根据username查询用户
        UserEntity user = authMapper.getByusername(username);

        //对比密码（用户名不存在也走同一句提示，避免暴露"这个用户名注册过"）
        //注意：现在库里 password_hash 存的是明文（例如 123456）。以后换成 BCrypt 等加密存储时，
        //这里要改成 passwordEncoder.matches(password, user.getPasswordHash())
        if (user == null || password == null || !password.equals(user.getPasswordHash())) {
            throw new ApiException("用户名或密码错误");
        }

        //建立登录态
        StpUtil.login(user.getId());

        //封装数据返回给前端
        SaTokenInfo token = StpUtil.getTokenInfo();
        return new LoginResponse(
                token.getTokenName(),
                token.getTokenValue(),
                token.getTokenTimeout()
                );
    }
}
