package jp.co.apidemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.security.config.authentication.UserServiceBeanDefinitionParser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jp.co.apidemo.entities.Employee;
import jp.co.apidemo.entities.Role;
import jp.co.apidemo.exception.EmployeeServiceException;
import jp.co.apidemo.repository.EmployeeRepository;
import jp.co.apidemo.repository.RoleRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;

import java.util.Optional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
@EnableCaching
public class EmployeeServiceImpl implements EmployeeService, UserDetailsService {
    
    private EmployeeRepository employeeRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository empRep, RoleRepository roleRep, PasswordEncoder passEnc) {
        this.employeeRepository = empRep;
        this.roleRepository = roleRep;
        this.passwordEncoder = passEnc;
    } 
    
    /*
     * <pre>
     * Assign roles to employe from role
     * </pre>
     * @param String name : Name of employee
     * @param String roleName : Name of role
     * @exception UsernameNotFoundException : Throws if employee do not exitst in database
     * @exception EmployeeServiceException : Throws if rolename do not exits in database
     * @return String : Added role information
     */
    @Override
    public String addRoleToEmployee(String name, String roleName) {
        log.info("Adding role {} to user {}", roleName, name);
        //Check if input name check 
        Employee employee = employeeRepository.findByName(name);
        if(employee==null)
        {
          throw new UsernameNotFoundException("Employee with name "+name+" not exists in database");
        }
        //Check if input rolename check
        Role role = roleRepository.findByName(roleName);
        if(role==null)
        {
          throw new EmployeeServiceException("Role with name "+roleName+" not exists in database");
        }
        employee.getRoles().add(role);
        employeeRepository.save(employee);
        return "Role "+roleName+" added to employee "+ name ;
    }

    /*
     * <pre>
     * Add role to database
     * </pre>
     * @param Role role : Role entity
     * @Return Role : Database saved entity
     */
    @Override
    public Role saveRole(Role role) {
        log.info("Saving new role {} to the database", role.getName());
        return roleRepository.save(role);
    }

     /*
     * <pre>
     * Add employee to database
     * </pre>
     * @param Employee employee : Employee entity
     * @Return Employee : Database saved entity
     */
    public Employee saveEmployee(Employee employee){
        log.info("Saving new employee {} to the database", employee.getName());
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    /*
     * <pre>
     * Fetch employee from database
     * Fetch all employee if input name field is null
     * Fetch employee in page of input size with descending order
     * </pre>
     * @param Sting name : Employee name
     * @param int page : page parameter
     * @param int size : page size limit
     * @param String[] sort : ssorting criteria,sorting order
     * @return Map<String,Object> : Return map with employee data along with paging metadata(currentpage info,total items info, total pages info.)
     */
    public Map<String, Object> retrieveEmployees(String name,int page,int size,String[] sort) {
        try {
          log.info("retrieveEmployees: Fetch data from database");
            List<Order> orders = new ArrayList<Order>();
            if (sort[0].contains(",")) {
              // will sort more than 2 fields
              // sortOrder="field, direction"
              for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
              }
            } else {
              // sort=[field, direction]
              orders.add(new Order(getSortDirection(sort[1]), sort[0]));
            }
            List<Employee> employees = new ArrayList<Employee>();
            Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));
            Page<Employee> pageEmployees;
            if (name == null)
            {
              pageEmployees = employeeRepository.findAll(pagingSort);
            }
            else
            {
            pageEmployees = employeeRepository.findByNameContaining(name, pagingSort);

            }
            employees = pageEmployees.getContent();
            
            if (employees.isEmpty()) {
              log.info("retrieveEmployees: No any data record exists");
              return null;
            }
            Map<String, Object> response = new HashMap<>();
            response.put("employees", employees);
            response.put("currentPage", (pageEmployees.getNumber()));
            System.out.println("currentpage "+pageEmployees.getNumber());
            response.put("totalItems", (pageEmployees.getTotalElements()));
            response.put("totalPages", (pageEmployees.getTotalPages()));
           
            return response;
          } catch (Exception e) {
            e.printStackTrace();
            return null;
          }
    }

    /*
     * <pre>
     * Fetch single employee with Id
     * Cache enabled
     * </pre>
     * @param Long employeeId : Employee Id
     * @exception EmployeeServiceException : If id do not exists in database
     * @return <Optional>Employee : Return single employee entity
     */
    @Cacheable(cacheNames = "employee", key="#employeeId")
    public Employee getEmployee(Long employeeId) {
        log.info("getEmployee: Fetch data from database");
        Optional<Employee> optEmp = employeeRepository.findById(employeeId);
        if(!optEmp.isPresent())
        {
            throw new EmployeeServiceException("Employee not found with id "+employeeId);
        }
        return optEmp.get();
    }


    /*
     * <pre>
     * Delete single employee with Id
     * Cache evict enabled
     * </pre>
     * @param Long employeeId : Employee Id
     * @exception EmployeeServiceException : If id do not exists in database
     * @return String : Return delete record information
     */
    @CacheEvict(cacheNames = "employee", key = "#employeeId")
    public String deleteEmployee(Long employeeId){
      Optional<Employee> employee = employeeRepository.findById(employeeId);
        if(!employee.isPresent())
        {
          throw new EmployeeServiceException("Employee with "+employeeId+ "not found");
        }
        employeeRepository.deleteById(employeeId);
        return "Employee "+ employeeId +" deleted";
    }

     /*
     * <pre>
     * Update single employee with Id
     * Cache put enabled
     * </pre>
     * @param Employee employeeIn
     * @param Long id : Employee Id
     * @exception EmployeeServiceException : If id do not exists in database
     * @return <Optional>Employee : Return single employee entity
     */
    @CachePut(cacheNames = "employee", key = "#employee.employeeIn")
    public Employee updateEmployee(Employee employeeIn,Long employeeId) {
     
      Optional<Employee> employeeData = employeeRepository.findById(employeeId);
      employeeIn.setPassword(passwordEncoder.encode(employeeIn.getPassword()));
      if (employeeData.isPresent()) {
        return employeeRepository.save(employeeIn);
      } else {
        throw new UsernameNotFoundException("Employee not found");
      }
    }

    /*
     * <pre>
     * Fetch single employee with name
     * Cache put enabled
     * </pre>
     * @param Employee employeeIn
     * @param Long id : Employee Id
     * @exception EmployeeServiceException : If id do not exists in database
     * @return <Optional>Employee : Return single employee entity
     */
    @Override
    public Employee getEmployeeByEmployeeName(String name) {
        log.info("getEmployeeByEmployeeName: Fetching user {}", name);
        Employee employee= employeeRepository.findByName(name);
        if(employee==null)
        {
          throw new UsernameNotFoundException("Employee "+ name+" not found ");
        }
        return employee;
    }

      /*
     * <pre>
     * Fetch single employee with name
     * Cache put enabled
     * </pre>
     * @param Employee employeeIn
     * @param Long id : Employee Id
     * @exception UsernameNotFoundException : If id do not exists in database
     * @return <Optional>Employee : Return single employee entity
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException
    {
       Employee employee= employeeRepository.findByName(name);
       if(employee==null)
       {
          log.info("Employee with username" +name+" not found in database");
          throw new UsernameNotFoundException("Employee with username not found in the database");       
       }
       else {
          log.info("Employee with username found in the database " + name);
       }
       Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
       employee.getRoles().forEach(role -> {
           authorities.add(new SimpleGrantedAuthority(role.getName()));
       });
       return new org.springframework.security.core.userdetails.User(employee.getName(),employee.getPassword(),authorities);
    }

     /*
     * <pre>
     * Method for sorting decision
     * </pre>
     * @param String direction : String value input for sorting (ASC/DESC)
     * @return Sor.Direction : Return sorting information.
     */
    private Sort.Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
          return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {
          return Sort.Direction.DESC;
        }
    
        return Sort.Direction.ASC;
      }
    
}
