PROBLEM: Creating a program for authentication when creating a small project is time consuming and unneccesary.

SOLUTION: Build a scalable library, based on Spring Boot, to manage authentication. 

FUTURE PLANS:
- Rate limiting (IP and account based)
- Multi-factor authentication support
- More extensive testing

`application.properties` file:
```properties
# Refresh token expires in 36000 seconds
cmr.easyauth.refreshExpirationTime=36000
# Access token expires in 3600 seconds
cmr.easyauth.accessExpirationTime=3600
# Defines the secret key for JWTs. May be left as AUTOGENERATE or omitted to create a random key every start-up
cmr.easyauth.jwtSecretKey=tfWgVzAMIFjnjfLheMw8npLyY/m7cz++clh3YUK/U+Q=
# (Optional) If headers are primarily used, set this to true (for optimization)
cmr.easyauth.prioritizeHeaders=true
# (Optional, IMPORTANT) Elevate a newly registered user with the specified username. Should be removed from production code properties, but it is necessary in order to gain and distribute higher-level permissions
cmr.easyauth.adminUsername=cmrboy26
# (Optional) default = true. If true, accessing 'GET /auth/login' will return a login webpage on the web, which allows a user to log in with a username and password and automatically store cookies. 
cmr.easyauth.enableLoginWebpage=true
```

### Setup
1. Import the library into your project's `build.gradle` file (will elaborate on this later)
2. Create the Application file, which will be where the Spring Boot project starts:
```java
@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = {"net.cmr.easyauth"})
@ComponentScan("net.cmr.easyauth")
@EnableJpaRepositories("net.cmr.easyauth")
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```
3. Create your `EALogin` extension, which holds data about each user. (The example here includes an optional, unique "email" and a necessary, non-unique "name").
```java
@Entity
@Table(name="example_users")
public class ExampleLogin extends EALogin {

    // Nullable = true means necessary at the time of registration
    // Unique will prevent other users from having the same value
    @Column(name = "email", nullable = true, unique = true)
    private String email;
    // Nullable = false means optional in regards to registering an account
    @Column(name = "name", nullable = false)
    private String name;

    // No-args constructor
    public ExampleLogin() { }

    // Constructor will be called when a user is being registered
    public ExampleLogin(NonNullMap<String, String> registerParams) throws NullValueException {
        super(registerParams);
        // For NULLABLE @Column parameters, use getNullable() instead of get()
        this.email = registerParams.getNullable("email");
        // When a key is not present or null, NullValueException will be thrown when get() is called
        this.name = registerParams.get("name");
    }

    // Getters and setters...
}
```java
@Service
public class ExampleLoginService extends EALoginService<ExampleLogin> {

    @Override
    protected ExampleLogin createNewInstance(NonNullMap<String, String> registerParams) {
        return new ExampleLogin(registerParams);
    }

    @Override
    public boolean overrideAreCredentialsUnique(NonNullMap<String, String> registerParams) {
        // Replace with custom logic for determining if passed credentials are unique
        // For this example, email is unique, but not necessary.
        return registerParams.getNullable("email") == null || getLoginRepository(ExampleRepository.class).findByEmail(registerParams.get("email")).isEmpty();
    }

    @Override
    public void overrideVerifyCredentials(L login, NonNullMap<String, String> loginParams) throws CredentialException, AdditionalStepsRequiredException {
        // Check additional login parameters to verify login is correct. If incorrect, throw CredentialException
        // An example of and additional parameter to check is a multi-factor authentication or an email code
        // If another step needs to be completed, in the case of an email code, send the code and then throw an AdditionalStepsRequiredException
    }

    // Example code:

    public boolean sendEmail(TestLogin login) {
        // Email sending logic here...
        return true;
    }

}
```
```java
@RestController
public class ExampleRestController extends EARestController<ExampleLogin> {
    // Define custom API calls here...

    // Example:

    // hasRole('USER') will only succeed if the user has an ACCESS token, not just a REFRESH token
    // hasAuthority('ACCESS') or hasAuthority('REFRESH') can be used as needed.
    @PreAuthorize("hasRole('USER')")
    // Specify endpoint
    @GetMapping("/sendEmail")
    // If the user is logged in (hasAuthority('ACCESS') is true or hasRole([Any Role Here]) is true),
    // TestLogin will be the login associated with the ACCESS token, otherwise null
    public ResponseEntity<String> sendEmail(@EasyAuth TestLogin login) {
        boolean emailSent = getLoginService(TestLoginService.class).sendEmail(login);
        if (emailSent) {
            return ResponseEntity.ok("Email sent to "+login.getEmail());
        } else {
            return ResponseEntity.badRequest().body("Email not sent to "+login.getEmail());
        }
    }
}
```
```java
@Repository
public interface ExampleRepository extends EALoginRepository<ExampleLogin> {
   Optional<ExampleLogin> findByEmail(String email);
}
```

## API Calls

`Content-Type: application/json`

### Request/Response Examples

#### Register User

- Only necessary variables need to be sent in the register method
- `username` and `password` are necessary by default, and in the example above, `email` is necessary. (Since `name` isn't necessary, it CAN be omitted upon registration).

```json
POST /auth/register
{
    "username": "john_doe",
    "password": "securePassword123",
    "email": "john@example.com",
    "name": "John Doe"
}
```
Response: `201 Created` (empty body)
- Must call the login endpoint to recieve JWTs and cookies

#### Login
```json
POST /auth/login
{
    "username": "john_doe",
    "password": "securePassword123"
}
```
Response:
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- Also sets tokens as HTTP-only cookies

#### Refresh Token
```
GET /auth/refresh
Authorization: Bearer <refresh_token>
```
Response:
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- Also sets new access token as HTTP-only cookie

#### Get User Authorities
```
GET /auth/authorities
Authorization: Bearer <access_token>
```
Response:
```json
["ROLE_USER", "ROLE_ADMIN"]
```

#### Admin - Get User

- May use request parameters `username` or `id` to access a user

```
GET /auth/admin/user?username=john_doe
Authorization: Bearer <access_token>
```
Response:
```json
{
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "name": "John Doe",
    "authorities": [
        {
            "id": 1,
            "authorityValue": "ROLE_USER"
        }
    ]
}
```

### Authentication Methods

The library supports two methods for sending tokens:

1. **HTTP-only Cookies** (Recommended for web apps)
   - Automatically set by login endpoints

2. **Authorization Headers** (Recommended for APIs)
   - `Authorization: Bearer <token>`

Configure preference in `application.properties`:
```properties
# Prioritize headers over cookies when both are present
cmr.easyauth.prioritizeHeaders=true
```