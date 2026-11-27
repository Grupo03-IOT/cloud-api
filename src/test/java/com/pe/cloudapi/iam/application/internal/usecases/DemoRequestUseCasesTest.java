package com.pe.cloudapi.iam.application.internal.usecases;

import com.pe.cloudapi.iam.domain.model.aggregates.DemoRequest;
import com.pe.cloudapi.iam.domain.model.commands.RequestDemoCommand;
import com.pe.cloudapi.iam.domain.ports.out.DemoRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Demo request use cases (US06)")
class DemoRequestUseCasesTest {

    private final DemoRequestRepository requests = mock(DemoRequestRepository.class);

    @Test
    @DisplayName("stores the trimmed data, a lowercase email and the moment of consent")
    void stores() {
        when(requests.save(any())).thenAnswer(call -> call.getArgument(0));

        DemoRequest saved = new RequestDemoUseCaseImpl(requests).execute(
                new RequestDemoCommand("  Ana Torres ", " Ana@Coworking.PE ", " Coworking Lima ", 5));

        assertThat(saved.getFullName()).isEqualTo("Ana Torres");
        assertThat(saved.getWorkEmail()).isEqualTo("ana@coworking.pe");
        assertThat(saved.getCompany()).isEqualTo("Coworking Lima");
        assertThat(saved.getRoomCount()).isEqualTo(5);
        assertThat(saved.getConsentedAt()).isNotNull().isEqualTo(saved.getCreatedAt());
    }

    @Test
    @DisplayName("lists what the repository returns")
    void lists() {
        DemoRequest stored = DemoRequest.builder().fullName("Ana").build();
        when(requests.findAllNewestFirst()).thenReturn(List.of(stored));

        assertThat(new ListDemoRequestsUseCaseImpl(requests).execute()).containsExactly(stored);
    }
}
