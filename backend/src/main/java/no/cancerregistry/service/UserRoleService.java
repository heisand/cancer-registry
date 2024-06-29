package no.cancerregistry.service;

import no.cancerregistry.exception.OverlappingRoleException;
import no.cancerregistry.model.RoleDTO;
import no.cancerregistry.model.UserRoleDTO;
import no.cancerregistry.model.UserWithRolesDTO;
import no.cancerregistry.repository.RoleRepository;
import no.cancerregistry.repository.UnitRepository;
import no.cancerregistry.repository.UserRepository;
import no.cancerregistry.repository.UserRoleRepository;
import no.cancerregistry.repository.entity.Role;
import no.cancerregistry.repository.entity.Unit;
import no.cancerregistry.repository.entity.User;
import no.cancerregistry.repository.entity.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final RoleRepository roleRepository;

    public UserRoleService(
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            UnitRepository unitRepository,
            RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.unitRepository = unitRepository;
        this.roleRepository = roleRepository;
    }

    public UserRoleDTO createUserRole(UserRoleDTO userRoleDTO) {
        User user = userRepository.findById(userRoleDTO.getUserId()).orElseThrow(() ->
                new RuntimeException("User not found"));
        Unit unit = unitRepository.findById(userRoleDTO.getUnitId()).orElseThrow(() ->
                new RuntimeException("Unit not found"));
        Role role = roleRepository.findById(userRoleDTO.getRoleId()).orElseThrow(() ->
                new RuntimeException("Role not found"));

        UserRole userRole = new UserRole();
        userRole.setVersion(1);
        userRole.setUser(user);
        userRole.setUnit(unit);
        userRole.setRole(role);
        userRole.setValidFrom(userRoleDTO.getValidFrom());
        userRole.setValidTo(userRoleDTO.getValidTo());

        if (hasOverlappingRole(userRole)) {
            throw new OverlappingRoleException("A valid user role already exists");
        }

        UserRole savedUserRole = userRoleRepository.save(userRole);

        return new UserRoleDTO(
                Optional.ofNullable(savedUserRole.getId()),
                Optional.ofNullable(savedUserRole.getVersion()),
                savedUserRole.getUser().getId(),
                savedUserRole.getUnit().getId(),
                savedUserRole.getRole().getId(),
                savedUserRole.getValidFrom(),
                savedUserRole.getValidTo()
        );
    }

    public UserRole updateUserRole(UserRoleDTO userRoleDTO) {
        User user = userRepository.findById(userRoleDTO.getUserId()).orElseThrow(() ->
                new RuntimeException("User not found"));
        Unit unit = unitRepository.findById(userRoleDTO.getUnitId()).orElseThrow(() ->
                new RuntimeException("Unit not found"));
        Role role = roleRepository.findById(userRoleDTO.getRoleId()).orElseThrow(() ->
                new RuntimeException("Role not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setUnit(unit);
        userRole.setRole(role);

        if (hasOverlappingRole(userRole)) {
            throw new OverlappingRoleException("A valid user role already exists");
        }

        UserRole userRoleToSave = new UserRole();

        // Only the valid from and valid to timestamps can be changed
        userRoleToSave.setValidFrom(userRoleDTO.getValidFrom());
        userRoleToSave.setValidTo(userRoleDTO.getValidTo());

        return userRoleRepository.save(userRoleToSave);
    }


    public List<UserWithRolesDTO> getUsersWithRolesByUnitId(Long unitId) {
        List<UserRole> userRoles = userRoleRepository.findUserRolesByUnitId(unitId);

        Map<User, List<UserRole>> userRolesMap = userRoles.stream()
                .collect(Collectors.groupingBy(UserRole::getUser));

        return userRolesMap.entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    List<RoleDTO> roles = entry.getValue().stream()
                            .map(userRole -> new RoleDTO(
                                    Optional.ofNullable(userRole.getId()),
                                    Optional.ofNullable(userRole.getVersion()),
                                    userRole.getRole().getName()))
                            .collect(Collectors.toList());
                    return new UserWithRolesDTO(user.getId(), user.getName(), roles);
                })
                .collect(Collectors.toList());
    }

    public List<UserRoleDTO> getValidUserRoles(
            Long userId,
            Long unitId,
            ZonedDateTime timeStamp) {
        List<UserRole> validUsers = userRoleRepository.findValidUserRoles(userId, unitId, timeStamp);

        return validUsers.stream().map(
                user -> new UserRoleDTO(
                        Optional.ofNullable(user.getId()),
                        Optional.ofNullable(user.getVersion()),
                        user.getUser().getId(),
                        user.getUnit().getId(),
                        user.getRole().getId(),
                        user.getValidFrom(),
                        user.getValidTo())
        ).collect(Collectors.toList());
    }

    public List<UserRoleDTO> getUserRoles() {
        List<UserRole> users = (List<UserRole>) userRoleRepository.findAll();

        return users.stream().map(
                user -> new UserRoleDTO(
                        Optional.ofNullable(user.getId()),
                        Optional.ofNullable(user.getVersion()),
                        user.getUser().getId(),
                        user.getUnit().getId(),
                        user.getRole().getId(),
                        user.getValidFrom(),
                        user.getValidTo())
        ).collect(Collectors.toList());
    }

    public boolean hasOverlappingRole(UserRole userRole) {
        return userRoleRepository.hasOverlappingUserRole(
                userRole.getUser().getId(),
                userRole.getUnit().getId(),
                userRole.getRole().getId(),
                userRole.getValidFrom(),
                userRole.getValidTo()
        );
    }
}