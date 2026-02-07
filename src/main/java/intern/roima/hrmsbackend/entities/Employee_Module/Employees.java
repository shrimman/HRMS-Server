package intern.roima.hrmsbackend.entities.Employee_Module;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "Employees")
public class Employees implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long EmployeeId;

    @Column(nullable = false, length = 100)
    private String FirstName;

    @Column(nullable = false, length = 100)
    private String LastName;

    @Email
    @Column(unique = true, length = 255, nullable = false)
    private String Email;

    @Column(unique = true, length = 255, nullable = false)
    private String PasswordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RoleId", referencedColumnName = "RoleId", nullable = false)
    private Roles role;

    @OneToOne()
    @JoinColumn(name = "ManagerId", referencedColumnName = "EmployeeId", nullable = true)
    private Employees manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DepartmentId", referencedColumnName = "DepartmentId", nullable = true)
    private Departments department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DesignationId", referencedColumnName = "DesignationId", nullable = true)
    private Designations designation;

    @Column(nullable = false)
    private LocalDate DateOfBirth;

    @Column(nullable = false)
    private LocalDate DateOfJoining;

    @Column(nullable = false)
    private String PhotoPath = "https://pngtree.com/freepng/male-company-employee-avatar-icon-wearing-a-necktie_8537621.html";

    @Column(nullable = false)
    private boolean IsActive = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.getRoleName());

        return List.of(authority);
    }

    @Override
    public @Nullable String getPassword() {
        return this.PasswordHash;
    }

    @Override
    public String getUsername() {
        return this.Email;
    }

}
