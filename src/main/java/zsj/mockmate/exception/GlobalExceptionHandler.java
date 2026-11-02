package zsj.mockmate.exception;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import zsj.mockmate.pojo.ApiException;
import zsj.mockmate.pojo.Result;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：统一 200 + code=0 + msg */
    @ExceptionHandler(ApiException.class)
    public Result<Void> handleApi(ApiException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 未登录 / token 失效：Sa-Token 拦截器抛的异常。
     *
     * 必须单独处理：如果落进下面的兜底，会变成 200 + "服务器内部错误"，
     * 前端就没法区分"没登录该跳登录页"和"接口真的炸了"。这里统一返回 HTTP 401。
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLogin(NotLoginException e) {
        log.debug("未登录访问：{}", e.getMessage());
        return Result.error("未登录或登录已过期，请重新登录");
    }

    /** 唯一键冲突：注册时用户名已存在 */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicate(DuplicateKeyException e) {
        return Result.error("用户名已存在");
    }

    /** 请求体不是合法 JSON / 字段类型不匹配 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return Result.error("请求参数格式不正确");
    }

    /** 参数校验失败（等你引了 spring-boot-starter-validation 才会触发） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.error(msg);
    }

    /**
     * 接口不存在（后端还没实现，或路径写错）。
     *
     * 必须单独处理：Spring 6.1+ 对"没有匹配的 handler / 静态资源"抛 NoResourceFoundException，
     * 如果落进下面的兜底，就会变成 200 + "服务器内部错误"，前端根本分不清是没实现还是真报错。
     */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(Exception e) {
        log.debug("接口不存在：{}", e.getMessage());
        return Result.error("接口不存在（尚未实现）");
    }

    /** 兜底：别把堆栈吐给前端，但要记日志 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("未处理异常", e);
        return Result.error("服务器内部错误");
    }
}