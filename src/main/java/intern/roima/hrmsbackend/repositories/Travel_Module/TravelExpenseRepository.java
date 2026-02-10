package intern.roima.hrmsbackend.repositories.Travel_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.TravelExpenses;

public interface TravelExpenseRepository extends JpaRepository<TravelExpenses, Long> {

}
