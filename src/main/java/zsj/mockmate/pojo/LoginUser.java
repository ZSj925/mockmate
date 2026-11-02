package zsj.mockmate.pojo;

/**
 * 当前登录用户（放进 SecurityContext 的 principal）
 * 控制器里可以这样拿：@AuthenticationPrincipal LoginUser user
 */
public record LoginUser(Long id, String username) {
}
