package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import intern.roima.hrmsbackend.entities.Travel_Module.TravelExpenses;

public interface TravelExpenseRepository extends JpaRepository<TravelExpenses, Long> {

    List<TravelExpenses> findByTravelPlan_TravelId(Long travelPlanId);

    List<TravelExpenses> findBySubmittedBy_EmployeeId(Long employeeId);

    List<TravelExpenses> findByApprovalStatus_StatusId(Long statusId);

    List<TravelExpenses> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM TravelExpenses e WHERE e.travelPlan.travelId = :travelPlanId")
    BigDecimal getTotalExpenseAmountByTravelPlan(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT SUM(e.amount) FROM TravelExpenses e WHERE e.travelPlan.travelId = :travelPlanId AND e.submittedBy.employeeId = :employeeId")
    BigDecimal getTotalExpenseAmountByTravelPlanAndEmployee(@Param("travelPlanId") Long travelPlanId, @Param("employeeId") Long employeeId);

}
