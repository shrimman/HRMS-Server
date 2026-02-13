package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.TravelPlans;

public interface TravelPlanRepository extends JpaRepository<TravelPlans, Long> {

    List<TravelPlans> findByCreatedByHR_EmployeeId(Long hrId);

}
