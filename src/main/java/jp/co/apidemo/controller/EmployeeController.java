package jp.co.apidemo.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.apidemo.entities.Employee;
import jp.co.apidemo.entities.Role;
import jp.co.apidemo.service.EmployeeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import org.springframework.cache.annotation.EnableCaching;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeController {
    private EmployeeService employeeService;

    @Autowired
    public void setEmployeeService(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    /*
     * <pre>
     * API : Fetch employees with name 
     * If name == null : retrive all employees data  in paging format by default sorted in descending format
     * </pre>
     * @RequestParam Sting name : Not mandatory , name of employee
     * @RequestParam int page : page parameter
     * @param int size : page size limit
     * @param String[] sort : sorting criteria,sorting order
     * @return Map<String,Object> : Return map with employee data along with paging metadata(currentpage info,total items info, total pages info.)
     */
    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getEmployees(
        @RequestParam(value = "name",required = false) String name,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size,
        @RequestParam(defaultValue = "id,desc") String[] sort
    ) {
        Map<String, Object> employees = employeeService.retrieveEmployees(name,page,size,sort);
        return new ResponseEntity<>(employees,HttpStatus.OK);
    }

    /*
     * <pre>
     * API : Fetch employees from database with employee id
     * </pre>
     * @PathVariable Long employeeId : Id for which data should be fetched
     * @return ResponseEntity<Employee> : return employee entity
     */
    @GetMapping("/employee/get/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable(name="employeeId")@Validated Long employeeId) {
        return ResponseEntity.ok().body(employeeService.getEmployee(employeeId));
    }

    /*
     * <pre>
     * API : Insert employee to database
     * </pre>
     * @RequestBody Employee employee : Employee entity in json format (Json format:{"name":"XXX","salary":100,"department":"XXX","password":"1234","roles":[]})
     * @return ResponseEntity<Employee> : return employee entity
     */
    @PostMapping("/employee/save")
    public ResponseEntity<Employee> saveEmployee(@Valid @RequestBody Employee employee) throws ParseException{
        return ResponseEntity.ok().body(employeeService.saveEmployee(employee));
    }

     /*
     * <pre>
     * API : Insert role to database
     * </pre>
     * @RequestBody Role role : Role entity in json format (Json format:{"name":"XXX"})
     * @return ResponseEntity<Role> : return role entity
     */
    @PostMapping("/role/save")
    public ResponseEntity<Role>saveRole(@RequestBody Role role) {
        return ResponseEntity.ok().body(employeeService.saveRole(role));
    }

     /*
     * <pre>
     * API : Delete employee from database with id
     * </pre>
     * @PathVariable Long employeeId : Id for which data should be deleted
     * @return ResponseEntity<String> : Deleted record information
     */
    @DeleteMapping("/employee/delete/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable(name="employeeId")Long employeeId){
        return ResponseEntity.ok().body(employeeService.deleteEmployee(employeeId));

    }

    /*
     * <pre>
     * API : Update employee from database 
     * </pre>
     * @PathVariable Long employeeId : Id for which data should be updated
     * @RequestBody Employee employee : Employee entity in json format (Json format:{"name":"XXX","salary":100,"department":"XXX","password":"1234","roles":[]})
     * @return ResponseEntity<Employee> : return updated employee entity
     */
    @PutMapping("/employee/update/{employeeId}")
    public ResponseEntity<Employee> updateEmployee(@RequestBody Employee employee, @PathVariable(name="employeeId")Long employeeId){
        Employee employeeData=employeeService.updateEmployee(employee,employeeId);
        return ResponseEntity.ok().body(employeeData);

    }

    @GetMapping("/token/refresh")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader != null && authorizationHeader.startsWith("employee ")) {
            try {
                String refresh_token = authorizationHeader.substring("Employee ".length());
                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(refresh_token);
                String username = decodedJWT.getSubject();
                Employee employee = employeeService.getEmployeeByEmployeeName(username);
                String access_token = JWT.create()
                    .withSubject(employee.getName())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
                    .withIssuer(request.getRequestURL().toString())
                    .withClaim("roles", employee.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                    .sign(algorithm);
                Map<String, String> tokens = new HashMap<>();
                tokens.put("access_token", access_token);
                tokens.put("refresh_token", refresh_token);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            }catch (Exception exception) {
                response.setHeader("error", exception.getMessage());
                response.setStatus(FORBIDDEN.value());
                //response.sendError(FORBIDDEN.value());
                Map<String, String> error = new HashMap<>();
                error.put("error_message", exception.getMessage());
                response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), error);
            }
        } else {
            throw new RuntimeException("Refresh token is missing");
        }
    }

     /*
     * <pre>
     * API : Assign role to employee
     * </pre>
     * @RequestBody RoleToUserForm form 
     * @return ResponseEntity<String> : Added role information
     */
    @PostMapping("/role/addtoemployee")
    public ResponseEntity<String>addRoleToEmployee(@RequestBody RoleToUserForm form) {
        String result =employeeService.addRoleToEmployee(form.getName(), form.getRoleName());
        return ResponseEntity.ok().body(result);
    }
}



@Data
class RoleToUserForm {
    private String name;
    private String roleName;
}