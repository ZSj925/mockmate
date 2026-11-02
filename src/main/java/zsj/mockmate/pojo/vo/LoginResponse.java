package zsj.mockmate.pojo.vo;

public record LoginResponse(String tokenName,
                            String tokenValue,
                            long tokenTimeout) {
}
