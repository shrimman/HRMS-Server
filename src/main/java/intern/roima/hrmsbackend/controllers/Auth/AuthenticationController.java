package intern.roima.hrmsbackend.controllers.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Requests.LoginRequest;
import intern.roima.hrmsbackend.dtos.Requests.RegistrationRequest;
import intern.roima.hrmsbackend.dtos.Responses.AuthenticationResponse;
import intern.roima.hrmsbackend.dtos.Responses.AuthenticationResponse.UserDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Employee_Module.Roles;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.RoleRepository;
import intern.roima.hrmsbackend.security.JwtUtility;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtility jwtUtility;
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtility jwtUtility,
            EmployeeRepository employeeRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtility = jwtUtility;
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        Employees employee = (Employees) authentication.getPrincipal();
        String token = jwtUtility.generateToken(employee);

        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setSuccess(true);
        authResponse.setMessage("Login successful");

        UserDto userDto = new UserDto();
        userDto.setEmployeeId(employee.getEmployeeId());
        userDto.setEmail(employee.getEmail());
        userDto.setFirstName(employee.getFirstName());
        userDto.setLastName(employee.getLastName());
        userDto.setRoleName(employee.getRole().getRoleName());
        authResponse.setUser(userDto);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signup(@Valid @RequestBody RegistrationRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Roles employeeRole = roleRepository.findByRoleName(request.getRoleName().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + request.getRoleName()));

        Employees employee = new Employees();
        employee.setEmail(request.getEmail());
        employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setRole(employeeRole);
        employee.setActive(true);

        Employees savedEmployee = employeeRepository.save(employee);

        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setSuccess(true);
        authResponse.setMessage("Registration successful");

        UserDto userDto = new UserDto();
        userDto.setEmployeeId(savedEmployee.getEmployeeId());
        userDto.setEmail(savedEmployee.getEmail());
        userDto.setFirstName(savedEmployee.getFirstName());
        userDto.setLastName(savedEmployee.getLastName());
        userDto.setRoleName(savedEmployee.getRole().getRoleName());
        authResponse.setUser(userDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticationResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Employees employee = (Employees) authentication.getPrincipal();

        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setSuccess(true);
        authResponse.setMessage("Current user retrieved successfully");

        UserDto userDto = new UserDto();
        userDto.setEmployeeId(employee.getEmployeeId());
        userDto.setEmail(employee.getEmail());
        userDto.setFirstName(employee.getFirstName());
        userDto.setLastName(employee.getLastName());
        userDto.setRoleName(employee.getRole().getRoleName());
        authResponse.setUser(userDto);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logout(HttpServletResponse response) {
        SecurityContextHolder.clearContext();

        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setSuccess(true);
        authResponse.setMessage("Logout successful");

        return ResponseEntity.ok(authResponse);
    }
}
