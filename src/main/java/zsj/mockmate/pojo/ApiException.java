package zsj.mockmate.pojo;

/**
 * 业务异常：用户名密码错、参数不合法之类，由 GlobalExceptionHandler 统一转成 Result
 */
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}