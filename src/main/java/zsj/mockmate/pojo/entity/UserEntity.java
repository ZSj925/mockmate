package zsj.mockmate.pojo.entity;

import lombok.Data;


@Data
public class UserEntity {

    private Integer id;//主键
    private String username;//用户名
    private String passwordHash;//密码
    private String nickname;//昵称
    private String createdAt;//创建日期

}
