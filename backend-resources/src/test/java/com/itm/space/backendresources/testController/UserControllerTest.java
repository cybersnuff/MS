package com.itm.space.backendresources.testController;


import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest extends BaseIntegrationTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    public void testCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "john_doe",
                "john.doe@example.com",
                "securePassword123",
                "John",
                "Doe");
        var request = requestWithContent(post("/api/users"), userRequest);
        Mockito
                .doNothing()        // создаем заглушки для метода, который должен ничего не делать
                .when(userService)     // когда мы юзаем юзерСервис
                .createUser(any(UserRequest.class));
        mvc.perform(request)
                .andExpect(status().isOk());
    }


    // случай когда забыли указать фамилию
    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    public void testCreateUserNegative() throws Exception {
        UserRequest userRequest = new UserRequest(
                "john_doe",
                "john.doe@example.com",
                "securePassword123",
                "John",
                "");
        var request = requestWithContent(post("/api/users"), userRequest);
        Mockito
                .doNothing()        // создаем заглушки для метода, который должен ничего не делать
                .when(userService)     // когда мы юзаем юзерСервис
                .createUser(any(UserRequest.class));
        mvc.perform(request)
                .andExpect(status().is4xxClientError());
    }


    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    public void testGetUserById() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse userResponse = new UserResponse(
                "Vadim",
                "Goshin",
                "myEmail.com",
                List.of("USER"),
                List.of("Manager"));
        Mockito.when(userService.getUserById(id)).thenReturn(userResponse);
        mvc.perform(get("/api/users/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("myEmail.com"))
                .andExpect(jsonPath("$.lastName").value("Goshin"))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    public void testGetUserByIdNegative() throws Exception {
        UUID id = UUID.randomUUID();
        // Указываем, что при вызове метода getUserById с неправильным id будет возвращаться null
        Mockito.when(userService.getUserById(id)).thenReturn(null);
        mvc.perform(get("/api/users/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                // Ожидаем статус "403 доступ запрещен, т.к USER пробует получить по ID"
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    public void testHello() throws Exception {
        mvc.perform(get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    // тест с негативным сценарием. Неправильная страница -> возвращает 400
    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    public void testHelloNegative() throws Exception {
        mvc.perform(get("/api/users/hello-not-existing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

}

