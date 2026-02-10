package intern.roima.hrmsbackend.repositories.Employee_Module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, Long> {

    Optional<Employees> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employees> findByManagerEmployeeId(Long managerId);

    List<Employees> findByDepartmentDepartmentId(Long departmentId);

    @Query("SELECT e FROM Employees e WHERE e.department.departmentName = :departmentName AND e.isActive = true")
    List<Employees> findByDepartmentName(@Param("departmentName") String departmentName);

    List<Employees> findByDesignationDesignationId(Long designationId);

    @Query("SELECT e FROM Employees e WHERE e.designation.designationName = :designationName AND e.isActive = true")
    List<Employees> findByDesignationName(@Param("designationName") String designationName);

    List<Employees> findByRoleRoleId(Long roleId);

    @Query("SELECT e FROM Employees e WHERE e.role.roleName = :roleName AND e.isActive = true")
    List<Employees> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT e FROM Employees e WHERE " +
            "e.isActive = true AND " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:department IS NULL OR :department = '' OR LOWER(e.department.departmentName) = LOWER(:department)) AND "
            +
            "(:designation IS NULL OR :designation = '' OR LOWER(e.designation.designationName) = LOWER(:designation)) AND "
            +
            "(:role IS NULL OR :role = '' OR LOWER(e.role.roleName) = LOWER(:role))")
    Page<Employees> searchEmployees(
            @Param("query") String query,
            @Param("department") String department,
            @Param("designation") String designation,
            @Param("role") String role,
            Pageable pageable);

    Page<Employees> findByIsActive(boolean isActive, Pageable pageable);

    List<Employees> findByIsActive(boolean isActive);

    long countByIsActive(boolean isActive);
}
