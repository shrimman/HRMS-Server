package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseReceipt;

public interface ExpenseReceiptRepository extends JpaRepository<ExpenseReceipt, Long> {

    List<ExpenseReceipt> findByExpense_ExpenseId(Long expenseId);

}
