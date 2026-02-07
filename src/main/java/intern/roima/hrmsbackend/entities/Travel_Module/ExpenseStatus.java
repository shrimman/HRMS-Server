package intern.roima.hrmsbackend.entities.Travel_Module;

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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ExpenseStatus")
public class ExpenseStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long StatusId;

    @Column(nullable = false)
    private String StatusName;
}
