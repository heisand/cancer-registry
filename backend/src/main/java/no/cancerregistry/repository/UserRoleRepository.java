package no.cancerregistry.repository;

import no.cancerregistry.repository.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface UserRoleRepository extends CrudRepository<UserRole, Long> {

    @Query("SELECT ur FROM UserRole ur JOIN FETCH ur.user u JOIN FETCH ur.role r WHERE ur.unit.id = :unitId")
    List<UserRole> findUserRolesByUnitId(Long unitId);

    @Query("SELECT COUNT(*)" +
            "FROM UserRole u " +
            "WHERE u.user.id = :userId " +
            "AND u.unit.id = :unitId " +
            "AND u.role.id = :roleId " +
            "AND (:validFrom BETWEEN u.validFrom AND u.validTo " +
            "     OR :validTo BETWEEN u.validFrom AND u.validTo)")
    Long hasOverlappingUserRole(
            Long userId,
            Long unitId,
            Long roleId,
            ZonedDateTime validFrom,
            ZonedDateTime validTo);
}
