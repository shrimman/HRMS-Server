package intern.roima.hrmsbackend.services.Travel_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.CreateTravelPlanRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateTravelPlanRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelPlanDto;

public interface TravelService {

    TravelPlanDto createTravelPlan(CreateTravelPlanRequest request, Long hrId);

    TravelPlanDto getTravelPlanById(Long travelPlanId);

    TravelPlanDto updateTravelPlan(Long travelPlanId, UpdateTravelPlanRequest request, Long hrId);

    void deleteTravelPlan(Long travelPlanId, Long hrId);

    List<TravelPlanDto> getAllTravelPlans();

    List<TravelPlanDto> getTravelPlansForEmployee(Long employeeId);

    List<TravelPlanDto> getTravelPlansCreatedByHR(Long hrId);

    List<EmployeeSummaryDto> getEmployeesForTravelPlan(Long travelPlanId);

    void addEmployeeToTravelPlan(Long travelPlanId, Long employeeId, Long hrId);

    void removeEmployeeFromTravelPlan(Long travelPlanId, Long employeeId, Long hrId);

    boolean canSubmitExpenseForTravel(Long travelPlanId, Long employeeId);

}
