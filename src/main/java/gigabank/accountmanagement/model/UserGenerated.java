
package gigabank.accountmanagement.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;


/**
 * User
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "password",
    "email",
    "phone"
})
@Data
public class UserGenerated {

    /**
     * User identifier
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("User identifier")
    public Integer id;
    /**
     * User login name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("User login name")
    public String name;
    /**
     * User password
     * 
     */
    @JsonProperty("password")
    @JsonPropertyDescription("User password")
    public String password;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    public String email;
    @JsonProperty("phone")
    public String phone = "+1234567890";

}
