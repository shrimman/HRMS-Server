package intern.roima.hrmsbackend.repositories.Employee_Module;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import intern.roima.hrmsbackend.entities.Employee_Module.Roles;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Integer> {
    boolean existsByRoleName(String roleName);

}
