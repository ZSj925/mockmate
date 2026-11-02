package zsj.mockmate.service;


import zsj.mockmate.pojo.vo.LoginResponse;

public interface authService {
    LoginResponse doLogin(String username, String password);
}
