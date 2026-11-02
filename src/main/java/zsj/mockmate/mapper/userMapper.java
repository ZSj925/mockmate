package zsj.mockmate.mapper;

import org.apache.ibatis.annotations.Mapper;
import zsj.mockmate.pojo.entity.UserEntity;

@Mapper
public interface UserMapper {

    //添加新用户
    void setUser(UserEntity user);
    //删除对应id的用户
    void deleteUser(Integer id);
    //修改用户（全部）
    void updateUser(UserEntity user);
    //根据id查询用户
    UserEntity getUser(Integer id);
    //根据username查询用户
    UserEntity getUserByName(String username);

}
