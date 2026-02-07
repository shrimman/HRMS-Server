package intern.roima.hrmsbackend.entities.JobModule;

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
@Table(name = "JobOpening")
public class JobOpenings {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long JobId;

    @Column(nullable = false, length = 255)
    private String Title;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String Summary;

    @Column(nullable = true, length = 255)
    private String JDFilePath;

    @Column(nullable = false)
    private Boolean IsActive;

    @Column(nullable = false, updatable = false)
    private LocalDateTime PostedAt;

}
