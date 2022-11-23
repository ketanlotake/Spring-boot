package jp.co.apidemo.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import jp.co.apidemo.entities.Employee;
import jp.co.apidemo.entities.Role;

@Component
public interface EmployeeService {

    /*
     * <pre>
     * Fetch employee from database
     * Fetch all employee if input name field is null
     * Fetch employee in page of input size with descending order
     * </pre>
     * @param Sting name : Employee name
     * @param int page : page parameter
     * @param int size : page size limit
     * @param String[] sort : sorting order
     * @return Map<String,Object> : Return map with employee data along with paging metadata(currentpage info,total items info, total pages info.)
     */
    public Map<String, Object> retrieveEmployees(String name,int page,int size,String[] sort);

     /*
     * <pre>
     * Fetch single employee with Id
     * Cache enabled
     * </pre>
     * @param Long employeeId : Employee Id
     * @exception EmployeeServiceException : If id do not exists in database
     * @return <Optional>Employee : Return single employee entity
     */
    public Employee getEmployee(Long employeeId);

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
    public Employee getEmployeeByEmployeeName(String name);

     /*
     * <pre>
     * Add employee to database
     * </pre>
     * @param Employee employee : Employee entity
     * @Return Employee : Database saved entity
     */
    public Employee saveEmployee(Employee employee) throws ParseException;

     /*
     * <pre>
     * Assign roles to employe from role
     * </pre>
     * @param String name : Name of employee
     * @param String roleName : Name of role
     * @exception UsernameNotFoundException : Throws if employee do not exitst in database
     * @exception EmployeeServiceException : Throws if rolename do not exits in database
     * @return String : Add role information
     */
    public String addRoleToEmployee(String name, String roleName);
    
     /*
     * <pre>
     * Add role to database
     * </pre>
     * @param Role role : Role entity
     * @Return Role : Database saved entity
     */
    public Role saveRole(Role role);
    
    /*
     * <pre>
     * Delete single employee with Id
     * Cache evict enabled
     * </pre>
     * @param Long employeeId : Employee Id
     * @exception EmployeeServiceException : If id do not exists in database
     * @return String : Return delete record information
     */
    public String deleteEmployee(Long employeeId);

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
    public Employee updateEmployee(Employee employee, Long employeeId);
}
