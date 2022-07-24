package web.cloudfilestorage.dto.user;

import lombok.Builder;
import lombok.Data;
import web.cloudfilestorage.model.Role;
import web.cloudfilestorage.model.Status;

import javax.validation.constraints.Null;
import java.util.List;

@Data
@Builder
public class UserUpdate {
    /*
    * User can not change email linked to account
    * */

    private String username;

    private String password;

    private String firstName;

    private String lastName;

}
