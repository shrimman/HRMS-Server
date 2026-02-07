package intern.roima.hrmsbackend.entities.Travel_Module;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ExpenseReceipt")
public class ExpenseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseReceiptId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 255)
    private String receiptPath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

}
