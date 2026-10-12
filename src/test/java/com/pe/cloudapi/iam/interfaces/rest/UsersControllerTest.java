package com.pe.cloudapi.iam.interfaces.rest;

import com.pe.cloudapi.iam.application.internal.ports.in.ListUsersUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RegisterUserUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.User;
import com.pe.cloudapi.iam.domain.model.valueobjects.Role;
import com.pe.cloudapi.iam.interfaces.rest.transform.UserResourceAssembler;
import com.pe.cloudapi.support.WebLayerTestConfiguration;
import com.pe.cloudapi.support.WebLayerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsersController.class)
@ContextConfiguration(classes = WebLayerTestConfiguration.class)
@Import({UsersController.class, UserResourceAssembler.class})
@DisplayName("GET /api/v1/users (list accounts)")
class UsersControllerTest extends WebLayerTestSupport {

    @MockitoBean private RegisterUserUseCase registerUser;
    @MockitoBean private ListUsersUseCase listUsers;

    @Test
    @DisplayName("200: an administrator lists accounts with their roles and no password")
    void adminListsUsers() throws Exception {
        when(listUsers.execute()).thenReturn(List.of(User.builder()
                .id(UUID.randomUUID()).email("member@sensework.test").passwordHash("hash")
                .displayName("Member").active(true).roles(Set.of(Role.MEMBER)).build()));

        mockMvc.perform(get("/api/v1/users").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("member@sensework.test"))
                .andExpect(jsonPath("$[0].roles[0]").value("MEMBER"))
                .andExpect(jsonPath("$[0].password_hash").doesNotExist());
    }

    @Test
    @DisplayName("403: a MEMBER cannot list accounts")
    void memberCannotList() throws Exception {
        mockMvc.perform(get("/api/v1/users").with(member()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(listUsers);
    }
}
