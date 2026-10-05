package com.example.backend.service;

import com.example.backend.dto.request.BranchRequest;
import com.example.backend.dto.response.BranchResponse;
import com.example.backend.model.Branch;
import com.example.backend.repository.BranchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    void getAllBranchesMapsActiveBranches() {
        Branch branch = branch("Main", "1 Main Street", "ACTIVE", "main.example.com");
        when(branchRepository.findAllActiveBranches()).thenReturn(List.of(branch));

        List<BranchResponse> responses = branchService.getAllBranches();

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.getId()).isEqualTo(branch.getId());
            assertThat(response.getName()).isEqualTo("Main");
            assertThat(response.getAddress()).isEqualTo("1 Main Street");
            assertThat(response.getStatus()).isEqualTo("ACTIVE");
            assertThat(response.getDomain()).isEqualTo("main.example.com");
        });
    }

    @Test
    void getBranchByIdThrowsWhenBranchDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(branchRepository.findOneActiveBranch(id)).thenReturn(List.of());

        assertThatThrownBy(() -> branchService.getBranchById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Branch not found with ID: " + id);
    }

    @Test
    void createBranchBuildsAndMapsSavedBranch() {
        BranchRequest request = new BranchRequest();
        request.setName("New Branch");
        request.setAddress("2 Main Street");
        request.setStatus("ACTIVE");
        request.setDomain("new.example.com");
        Branch savedBranch = branch("New Branch", "2 Main Street", "ACTIVE", "new.example.com");
        when(branchRepository.save(any(Branch.class))).thenReturn(savedBranch);

        BranchResponse response = branchService.createBranch(request);

        ArgumentCaptor<Branch> captor = ArgumentCaptor.forClass(Branch.class);
        verify(branchRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Branch");
        assertThat(captor.getValue().getAddress()).isEqualTo("2 Main Street");
        assertThat(response.getId()).isEqualTo(savedBranch.getId());
        assertThat(response.getName()).isEqualTo("New Branch");
    }

    @Test
    void updateBranchPreservesStatusAndDomainWhenRequestValuesAreBlank() {
        UUID id = UUID.randomUUID();
        Branch branch = branch("Old Name", "Old Address", "ACTIVE", "old.example.com");
        branch.setId(id);
        BranchRequest request = new BranchRequest();
        request.setName("Updated Name");
        request.setAddress("Updated Address");
        request.setStatus(" ");
        request.setDomain(null);
        when(branchRepository.findOneActiveBranch(id)).thenReturn(List.of(branch));
        when(branchRepository.save(branch)).thenReturn(branch);

        BranchResponse response = branchService.updateBranch(id, request);

        assertThat(branch.getName()).isEqualTo("Updated Name");
        assertThat(branch.getAddress()).isEqualTo("Updated Address");
        assertThat(branch.getStatus()).isEqualTo("ACTIVE");
        assertThat(branch.getDomain()).isEqualTo("old.example.com");
        assertThat(branch.getUpdatedAt()).isNotNull();
        assertThat(branch.getUpdatedBy()).isEqualTo("system");
        assertThat(response.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteBranchMarksBranchDeletedAndSavesIt() {
        UUID id = UUID.randomUUID();
        Branch branch = branch("Main", "1 Main Street", "ACTIVE", "main.example.com");
        branch.setId(id);
        when(branchRepository.findOneActiveBranch(id)).thenReturn(List.of(branch));
        when(branchRepository.save(branch)).thenReturn(branch);

        BranchResponse response = branchService.deleteBranch(id);

        assertThat(branch.getDeletedAt()).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        verify(branchRepository).save(branch);
    }

    private Branch branch(String name, String address, String status, String domain) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 3, 12, 0);
        return Branch.builder()
                .id(UUID.randomUUID())
                .name(name)
                .address(address)
                .status(status)
                .domain(domain)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
