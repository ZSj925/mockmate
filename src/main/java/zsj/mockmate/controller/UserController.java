package zsj.mockmate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import zsj.mockmate.pojo.Result;
import zsj.mockmate.pojo.entity.UserEntity;
import zsj.mockmate.service.impl.UserServiceImpl;
import zsj.mockmate.service.UserService;

/**
 * 用户相关的接口
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    public UserService userService;
    //构造器注入
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 添加新用户
     * @param user
     * @return
     */
    @PostMapping()
    public Result<UserEntity> setUser(@RequestBody UserEntity user){
        log.info("新增用户:{}", user);
        userService.setUser(user);
        return Result.success();
    }

    /**
     * 根据id删除用户
     * @param id
     * @return
     */
    @DeleteMapping()
    public Result deleteUser(@RequestParam Integer id){
        log.info("删除id为{}用户",id);
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 修改用户（全部修改）
     * @param user
     * @return
     */
    @PutMapping()
    public Result updateUser(@RequestBody UserEntity user){
        log.info("修改的用户id:{}",user.getId());
        userService.updateUser(user);
        return Result.success();
    }

    /**
     * 查询对应id的用户
     * @param id
     * @return
     */
    @GetMapping()
    public Result<UserEntity> getUser(@RequestParam Integer id){
        log.info("要查询用户的id:{}",id);
        UserEntity user = userService.getUser(id);
        return Result.success(user);
    }

}
