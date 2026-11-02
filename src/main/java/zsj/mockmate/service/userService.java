package zsj.mockmate.service;

import org.springframework.stereotype.Service;
import zsj.mockmate.pojo.entity.UserEntity;

public interface UserService {
    /**
     * 新增用户
     * @param user
     */
    void setUser(UserEntity user);

    /**
     * 删除用户
     * @param id
     */
    void deleteUser(Integer id);

    /**
     * 修改用户
     * @param user
     */
    void updateUser(UserEntity user);

    /**
     * 查询对应id的用户
     * @param id
     * @return
     */
    UserEntity getUser(Integer id);
}
