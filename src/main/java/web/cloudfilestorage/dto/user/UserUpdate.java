package web.cloudfilestorage.dto.user;

import lombok.Builder;
import lombok.Data;

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
