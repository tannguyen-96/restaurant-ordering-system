package com.example.backend.service;

import com.example.backend.dto.request.RoleRequest;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.model.Policy;
import com.example.backend.model.Role;
import com.example.backend.repository.PolicyRepository;
import com.example.backend.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void createRoleRequiresExistingPoliciesAndMapsThem() {
        UUID policyId = UUID.randomUUID();
        Policy policy = policy(policyId, "branch:list");
        RoleRequest request = request("manager", List.of(policyId));
        Role savedRole = role("manager", Set.of(policy));
        when(policyRepository.findAllByIdIn(List.of(policyId))).thenReturn(List.of(policy));
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

        RoleResponse response = roleService.createRole(request);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertThat(captor.getValue().getPolicies()).containsExactly(policy);
        assertThat(response.getName()).isEqualTo("manager");
        assertThat(response.getPolicies()).singleElement()
                .satisfies(mapped -> assertThat(mapped.getName()).isEqualTo("branch:list"));
    }

    @Test
    void createRoleRejectsMissingPolicy() {
        UUID policyId = UUID.randomUUID();
        when(policyRepository.findAllByIdIn(List.of(policyId))).thenReturn(List.of());

        assertThatThrownBy(() -> roleService.createRole(request("manager", List.of(policyId))))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("One or more policies were not found");
    }

    @Test
    void getRoleByIdMapsRoleAndPolicies() {
        UUID id = UUID.randomUUID();
        Policy policy = policy(UUID.randomUUID(), "branch:get");
        Role role = role("admin", Set.of(policy));
        role.setId(id);
        when(roleRepository.findOneActiveRole(id)).thenReturn(List.of(role));

        RoleResponse response = roleService.getRoleById(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getPolicies()).extracting(policyResponse -> policyResponse.getName())
                .containsExactly("branch:get");
    }

    @Test
    void deleteRoleSetsDeleteDate() {
        UUID id = UUID.randomUUID();
        Role role = role("manager", Set.of());
        role.setId(id);
        when(roleRepository.findOneActiveRole(id)).thenReturn(List.of(role));

        roleService.deleteRole(id);

        assertThat(role.getDeleteDate()).isNotNull();
        verify(roleRepository).save(role);
    }

    private RoleRequest request(String name, List<UUID> policyIds) {
        RoleRequest request = new RoleRequest();
        request.setName(name);
        request.setDescription("Role description");
        request.setPolicyIds(policyIds);
        return request;
    }

    private Policy policy(UUID id, String name) {
        return Policy.builder()
                .id(id)
                .name(name)
                .service("branch")
                .action(name.substring(name.indexOf(':') + 1))
                .build();
    }

    private Role role(String name, Set<Policy> policies) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 3, 12, 0);
        return Role.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("Role description")
                .status("active")
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .policies(policies)
                .build();
    }
}
