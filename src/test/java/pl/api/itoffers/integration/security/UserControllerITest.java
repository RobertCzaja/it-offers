package pl.api.itoffers.integration.security;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.api.itoffers.helper.AbstractITest;
import pl.api.itoffers.helper.ApiAuthorizationHelper;
import pl.api.itoffers.helper.AuthorizationCredentials;
import pl.api.itoffers.helper.UserFactory;
import pl.api.itoffers.security.application.repository.UserRepository;
import pl.api.itoffers.security.ui.controller.UserController;
import pl.api.itoffers.security.ui.request.CreateUserRequest;
import pl.api.itoffers.security.ui.response.UserCreated;
import org.springframework.test.context.DynamicPropertyRegistry;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
public class UserControllerITest {

    @Autowired
    private TestRestTemplate template;
    @Autowired
    private ApiAuthorizationHelper apiAuthorizationHelper;
    @Autowired
    @Qualifier("postgreSQL")
    private UserRepository userRepository;


    @LocalServerPort
    private Integer port;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        userRepository.deleteAll();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    public void shouldCreateUser() {

        CreateUserRequest requestBody = UserFactory.createUserRequest();
        HttpEntity<CreateUserRequest> request = new HttpEntity<>(requestBody, apiAuthorizationHelper.getHeaders(AuthorizationCredentials.ADMIN));

        ResponseEntity<UserCreated> response = template.postForEntity(UserController.PATH, request, UserCreated.class);

        assertThat(response.getBody().getMessage()).contains("User created");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userRepository.findUserByEmail(requestBody.getEmail())).isNotNull();

        ResponseEntity<String> responseForDuplicatedRequest = template.postForEntity(UserController.PATH, request, String.class);
        assertThat(responseForDuplicatedRequest.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void shouldNotCreateUserDueToInvalidEmail() {

        CreateUserRequest requestBody = UserFactory.createUserRequest("someInvalidEmail");
        HttpEntity<CreateUserRequest> request = new HttpEntity<>(requestBody, apiAuthorizationHelper.getHeaders(AuthorizationCredentials.ADMIN));

        ResponseEntity<UserCreated> response = template.postForEntity(UserController.PATH, request, UserCreated.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldNotAllowToCreateUserLoggedUserWithRoleUser() {

        CreateUserRequest requestBody = UserFactory.createUserRequest();
        HttpEntity<CreateUserRequest> request = new HttpEntity<>(requestBody, apiAuthorizationHelper.getHeaders(AuthorizationCredentials.USER));

        ResponseEntity<UserCreated> response = template.postForEntity(UserController.PATH, request, UserCreated.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }
}
