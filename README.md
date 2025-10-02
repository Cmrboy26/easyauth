PROBLEM: Creating a program for authentication when creating a small project is time consuming and unneccesary.

SOLUTION: Build a scalable library, based on Spring Boot, to manage authentication like this. 

`application.properties` file:
```properties
# Any JWT distributed will expire in 600 seconds (10 minutes) 
net.cmr.easyauth.jwtExpirationSeconds=600
# Will create a new Base64-encoded HS256 SecretKey for JWT every time. If you wish to make it permanent, it will be in the console, where you can replace "AUTOGENERATE"
net.cmr.easyauth.jwtSecretKey=AUTOGENERATE
```

## Quickstart

```properties
spring.application.name=APPLICATION_NAME_HERE
spring.datasource.url=jdbc:mysql://URL_HERE/DATABASE_NAME
spring.datasource.driver-class-name=DRIVER_CLASS
spring.datasource.username=DATABASE_USERNAME
spring.datasource.password=DATABASE_PASSWORD
# Automatically sets up tables
spring.jpa.hibernate.ddl-auto=update

```

Create the Spring Boot application entry point.
```java
@SpringBootApplication
@EnableAsync
public class EasyAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyAuthApplication.class, args);
    }
}
```
Create the REST controller to create the authentication endpoints.
```java
import org.springframework.web.bind.annotation.RestController;
import net.cmr.easyauth.controller.AbstractAuthenticationController;

@RestController
public class ExampleController extends AbstractAuthenticationController {
    
}
```
AuthContext can be garunteed to be true if hasRole('USER') is present

### Authentication Endpoints

NOTE: All `USER` roles require the `Authorization` header to contain the JWT in this format: `Authorization: Bearer jwtHere`.
<table>
  <thead>
    <tr>
      <th>Endpoint</th>
      <th>Method</th>
      <th>Role</th>
      <th>Description</th>
      <th>Request Class</th>
      <th>Response Class</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>/auth/register</code></td>
      <td>POST</td>
      <td>Any</td>
      <td>Creates new user with role <code>USER</code></td>
      <td>RegisterRequest</td>
      <td>String (T/F)</td>
    </tr>
    <tr>
      <td><code>/auth/login</code></td>
      <td>POST</td>
      <td>Any</td>
      <td>Creates and provides JWT token</td>
      <td>LoginRequest</td>
      <td>SessionInfo**</td>
    </tr>
    <tr>
      <td><code>/auth/verify</code></td>
      <td>POST</td>
      <td>Any</td>
      <td>Return "true" if provided JWT is active/not expired.</td>
      <td>SessionInfo</td>
      <td>String (T/F)</td>
    </tr>
    <tr>
      <td><code>/auth/verify</code></td>
      <td>GET</td>
      <td>USER</td>
      <td>Returns "true" if provided JWT is active/ not expired.</td>
      <td>-*</td>
      <td>String (T/F)</td>
    </tr>
    <tr>
      <td><code>/auth/role</code></td>
      <td>GET</td>
      <td>USER</td>
      <td>Returns the name of the logged-in user's role.</td>
      <td>-*</td>
      <td>String (Role)</td>
    </tr>
    <tr>
      <td><code>/auth/admin/all?page=X&entries=Y</code></td>
      <td>GET</td>
      <td>ADMIN</td>
      <td>Lists Y entries on page X, sorted by id</td>
      <td>-*</td>
      <td>Login (List)</td>
    </tr>
  </tbody>
</table>

\* = A form of authentication must be passed along as well. This can either be in the `Authorization: Bearer jwtHere` format OR a `jwt-token` cookie, both provided by `/auth/login`
<br>
\*\* = Additionally returns a cookie titled `jwt-token` with the JWT token as the value. May be passed in as authentication for endpoints with bodies containing an "\*" above or, generally, with role requirements other than "Any".