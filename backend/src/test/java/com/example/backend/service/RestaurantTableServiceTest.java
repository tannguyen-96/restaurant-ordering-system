package com.example.backend.service;

import com.example.backend.dto.request.RestaurantTableRequest;
import com.example.backend.dto.response.RestaurantTableResponse;
import com.example.backend.model.Branch;
import com.example.backend.model.RestaurantTable;
import com.example.backend.repository.BranchRepository;
import com.example.backend.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantTableServiceTest {

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private RestaurantTableService tableService;

    @Test
    void getAllTablesMapsActiveTables() {
        Branch branch = branch();
        RestaurantTable table = table("T-01", branch, "AVAILABLE");
        when(tableRepository.findAllActiveTables()).thenReturn(List.of(table));

        List<RestaurantTableResponse> responses = tableService.getAllTables();

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(table.getId());
            assertThat(response.getCode()).isEqualTo("T-01");
            assertThat(response.getBranchId()).isEqualTo(branch.getId());
            assertThat(response.getQrToken()).isEqualTo("QR-TOKEN");
            assertThat(response.getStatus()).isEqualTo("AVAILABLE");
        });
    }

    @Test
    void getTableByIdThrowsWhenTableDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(tableRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.getTableById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Table not found with ID: " + id);
    }

    @Test
    void createTableBuildsTableWithGeneratedQrToken() {
        Branch branch = branch();
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setCode("T-02");
        request.setBranchId(branch.getId());
        request.setStatus("AVAILABLE");
        when(branchRepository.findById(branch.getId())).thenReturn(Optional.of(branch));
        when(tableRepository.save(any(RestaurantTable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantTableResponse response = tableService.createTable(request);

        ArgumentCaptor<RestaurantTable> captor = ArgumentCaptor.forClass(RestaurantTable.class);
        verify(tableRepository).save(captor.capture());
        RestaurantTable savedTable = captor.getValue();
        assertThat(savedTable.getCode()).isEqualTo("T-02");
        assertThat(savedTable.getBranch()).isSameAs(branch);
        assertThat(savedTable.getQrToken()).hasSize(10).matches("[A-Za-z0-9]+");
        assertThat(response.getQrToken()).isEqualTo(savedTable.getQrToken());
    }

    @Test
    void updateTableReplacesCodeBranchAndStatus() {
        Branch originalBranch = branch();
        Branch updatedBranch = branch();
        UUID tableId = UUID.randomUUID();
        RestaurantTable table = table("T-01", originalBranch, "AVAILABLE");
        table.setId(tableId);
        RestaurantTableRequest request = new RestaurantTableRequest();
        request.setCode("T-99");
        request.setBranchId(updatedBranch.getId());
        request.setStatus("OCCUPIED");
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));
        when(branchRepository.findById(updatedBranch.getId())).thenReturn(Optional.of(updatedBranch));
        when(tableRepository.save(table)).thenReturn(table);

        RestaurantTableResponse response = tableService.updateTable(tableId, request);

        assertThat(table.getCode()).isEqualTo("T-99");
        assertThat(table.getBranch()).isSameAs(updatedBranch);
        assertThat(table.getStatus()).isEqualTo("OCCUPIED");
        assertThat(response.getBranchId()).isEqualTo(updatedBranch.getId());
        assertThat(response.getQrToken()).isEqualTo("QR-TOKEN");
    }

    @Test
    void deleteTableDeletesFoundTable() {
        UUID tableId = UUID.randomUUID();
        RestaurantTable table = table("T-01", branch(), "AVAILABLE");
        table.setId(tableId);
        when(tableRepository.findById(tableId)).thenReturn(Optional.of(table));

        tableService.deleteTable(tableId);

        verify(tableRepository).delete(table);
    }

    private Branch branch() {
        return Branch.builder().id(UUID.randomUUID()).name("Main").build();
    }

    private RestaurantTable table(String code, Branch branch, String status) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 3, 12, 0);
        return RestaurantTable.builder()
                .id(UUID.randomUUID())
                .code(code)
                .branch(branch)
                .qrToken("QR-TOKEN")
                .status(status)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
