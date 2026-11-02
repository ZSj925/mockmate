package zsj.mockmate.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zsj.mockmate.mapper.UserMapper;
import zsj.mockmate.pojo.Result;
import zsj.mockmate.pojo.entity.UserEntity;
import zsj.mockmate.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    public UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 新增用户的业务方法
     * @param user
     */
    @Override
    public void setUser(UserEntity user) {
        user.setCreatedAt(LocalDateTime.now()+" ");
        userMapper.setUser(user);
    }

    /**
     * 删除用户的业务层方法
     * @param id
     */
    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteUser(id);
    }


    /**
     * 修改用户（全部修改）业务层方法
     * @param user
     */
    @Override
    public void updateUser(UserEntity user) {
        user.setCreatedAt(LocalDateTime.now()+" ");
        userMapper.updateUser(user);
    }

    /**
     * 查询对应id的用户
     * @param id
     * @return
     */
    @Override
    public UserEntity getUser(Integer id) {
        return userMapper.getUser(id);
    }
}
