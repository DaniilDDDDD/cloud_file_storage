package web.cloudfilestorage.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserLogin {
    @NotNull(
            groups = {OnRequest.class, OnResponse.class},
            message = "login field is not provided!"
    )
    private String login;  // username or email as login

    @NotNull(
            groups = OnRequest.class,
            message = "password field is not provided!"
    )
    private String password;

    @NotNull(
            groups = OnResponse.class
    )
    private String token;

    public interface OnRequest {}

    public interface OnResponse {}

}
