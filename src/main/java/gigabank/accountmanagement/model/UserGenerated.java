
package gigabank.accountmanagement.model;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


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
@Generated("jsonschema2pojo")
public class UserGenerated {

    /**
     * User identifier
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("User identifier")
    private Integer id;
    /**
     * User login name
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("User login name")
    private String name;
    /**
     * User password
     * 
     */
    @JsonProperty("password")
    @JsonPropertyDescription("User password")
    private String password;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone = "+1234567890";

    /**
     * User identifier
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * User identifier
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * User login name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * User login name
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * User password
     * 
     */
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    /**
     * User password
     * 
     */
    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

}
