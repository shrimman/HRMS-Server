package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseParticipants;

public interface ExpenseParticipantsRepository extends JpaRepository<ExpenseParticipants, Long> {

    List<ExpenseParticipants> findByExpense_ExpenseId(Long expenseId);

    List<ExpenseParticipants> findByEmployee_EmployeeId(Long employeeId);

    void deleteByParticipantId(Long participantId);

}
