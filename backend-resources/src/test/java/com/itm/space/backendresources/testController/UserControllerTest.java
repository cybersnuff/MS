package com.itm.space.backendresources.testController;


import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;

import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private Keycloak keycloak;

    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    @Order(0)
    void testCreateUser() throws Exception {
        var request = requestWithContent(post("/api/users"), createUserRequest());
        mvc.perform(request)
                .andExpect(status().isOk());
    }

    // случай когда забыли указать фамилию
    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    @Order(5)
    void testCreateUserNegative() throws Exception {
        cleanUp();
        var request = requestWithContent(post("/api/users"), createUserRequestForNegative());
        mvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    @Order(1)
    void testGetUserById() throws Exception {
        String id = keycloak.realm("ITM").users().search("Vadim").get(0).getId();
        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@email.com"))
                .andExpect(jsonPath("$.lastName").value("Goshin"))
                .andExpect(jsonPath("$.firstName").value("Vadim"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @Order(3)
    void testGetUserByIdNegative() throws Exception {
        String id = keycloak.realm("ITM").users().search("Vadim").get(0).getId(); // Поиск созданного пользователя в Keycloak
        mvc.perform(get("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON))
                // Ожидаем статус "403 доступ запрещен, т.к USER пробует получить по ID"
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    @Order(2)
    void testHello() throws Exception {
        mvc.perform(get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    // тест с негативным сценарием. Неправильная страница
    @Test
    @WithMockUser(username = "testUser", roles = {"MODERATOR"})
    @Order(4)
    void testHelloNegative() throws Exception {
        mvc.perform(get("/api/users/hello-not-existing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private UserRequest createUserRequest() {
        return new UserRequest("Vadim", "email@email.com", "121212",
                "Vadim", "Goshin");
    }

    private UserRequest createUserRequestForNegative() {
        return new UserRequest("zxc", "email@email.com", "121212",
                "Vadim", "");
    }

    // Объявление метода, который будет выполняться после каждого тестового метода
    void cleanUp() { // Начало метода очистки
        UserRepresentation userRepresentation = keycloak.realm("ITM").users().search("Vadim").get(0); // Поиск созданного пользователя в Keycloak
        keycloak.realm("ITM").users().get(userRepresentation.getId()).remove(); // Удаление созданного пользователя
    }

}

