package intern.roima.hrmsbackend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        try {

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

            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        } 
        catch (AuthenticationException e) {
            AuthenticationResponse errorResponse = new AuthenticationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Authentication failed: Invalid email or password");
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signup(@Valid @RequestBody RegistrationRequest request) {
        try {

            if (employeeRepository.existsByEmail(request.getEmail())) {
                AuthenticationResponse errorResponse = new AuthenticationResponse();
                errorResponse.setSuccess(false);
                errorResponse.setMessage("Email already registered");
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
            }

            Roles employeeRole = roleRepository.findByRoleName(request.getRoleName().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Default role not found"));

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

            return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
        } 
        catch (Exception e) {
            AuthenticationResponse errorResponse = new AuthenticationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Registration failed: " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
