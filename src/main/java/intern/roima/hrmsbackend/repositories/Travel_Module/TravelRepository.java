package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.Travels;

public interface  TravelRepository extends JpaRepository<Travels, Long>{

    List<Travels> findByEmployee_EmployeeId(Long employeeId);

    List<Travels> findByTravelPlan_TravelId(Long travelPlanId);

    Optional<Travels> findByTravelPlan_TravelIdAndEmployee_EmployeeId(Long travelPlanId, Long employeeId);
    
}
