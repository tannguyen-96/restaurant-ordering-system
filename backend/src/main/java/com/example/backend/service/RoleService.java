package com.example.backend.service;

import com.example.backend.dto.request.RoleRequest;
import com.example.backend.dto.response.PolicyResponse;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.helper.StringHelper;
import com.example.backend.model.Policy;
import com.example.backend.model.Role;
import com.example.backend.repository.PolicyRepository;
import com.example.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PolicyRepository policyRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAllActiveRoles().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        return mapToResponse(findRole(id));
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(StringHelper.defaultValue(request.getStatus(), "active"))
                .policies(new HashSet<>(findPolicies(request.getPolicyIds())))
                .build();
        return mapToResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(UUID id, RoleRequest request) {
        Role role = findRole(id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setStatus(StringHelper.defaultValue(request.getStatus(), role.getStatus()));
        role.setPolicies(new HashSet<>(findPolicies(request.getPolicyIds())));
        role.setUpdatedAt(LocalDateTime.now());
        role.setUpdatedBy("system");
        return mapToResponse(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(UUID id) {
        Role role = findRole(id);
        role.setDeleteDate(LocalDateTime.now());
        roleRepository.save(role);
    }

    private Role findRole(UUID id) {
        return roleRepository.findOneActiveRole(id).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
    }

    private List<Policy> findPolicies(List<UUID> policyIds) {
        List<Policy> policies = policyRepository.findAllByIdIn(policyIds);
        if (policies.size() != policyIds.stream().distinct().count()) {
            throw new RuntimeException("One or more policies were not found");
        }
        return policies;
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .status(role.getStatus())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .policies(role.getPolicies().stream()
                        .map(this::mapPolicy)
                        .collect(Collectors.toList()))
                .build();
    }

    private PolicyResponse mapPolicy(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .name(policy.getName())
                .service(policy.getService())
                .action(policy.getAction())
                .build();
    }
}
