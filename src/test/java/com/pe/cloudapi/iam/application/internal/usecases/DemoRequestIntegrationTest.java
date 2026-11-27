package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.application.internal.ports.in.ListDemoRequestsUseCase;
import com.pe.cloudapi.iam.application.internal.ports.in.RequestDemoUseCase;
import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** US06 against PostgreSQL: the V2 migration of iam and the entity agree. */
@SpringBootTest
@Transactional
@DisplayName("Demo requests on PostgreSQL (US06)")
class DemoRequestIntegrationTest {

    @Autowired RequestDemoUseCase request;
    @Autowired ListDemoRequestsUseCase list;

    @Test
    @DisplayName("a stored request can be read back")
    void storeAndList() {
        String email = "demo-" + UUID.randomUUID() + "@coworking.pe";

        DemoRequest saved = request.execute(new RequestDemoCommand("Ana Torres", email, "Coworking Lima", 5));

        assertThat(saved.getId()).isNotNull();
        assertThat(list.execute())
                .filteredOn(r -> r.getWorkEmail().equals(email))
                .singleElement()
                .satisfies(r -> {
                    assertThat(r.getRoomCount()).isEqualTo(5);
                    assertThat(r.getConsentedAt()).isNotNull();
                });
    }
}
