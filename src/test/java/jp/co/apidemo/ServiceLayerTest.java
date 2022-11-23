package jp.co.apidemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.apidemo.config.CacheConfig;
import jp.co.apidemo.entities.Employee;
import jp.co.apidemo.entities.Role;
import jp.co.apidemo.exception.EmployeeServiceException;
import jp.co.apidemo.repository.EmployeeRepository;
import jp.co.apidemo.repository.RoleRepository;
import jp.co.apidemo.service.EmployeeServiceImpl;
import net.sf.ehcache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.junit.jupiter.api.AfterAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import javax.persistence.Entity;

@ExtendWith(MockitoExtension.class)

public class ServiceLayerTest {

    @InjectMocks // This allows to inject Mock objects.
    EmployeeServiceImpl employeeServiceImpl;

	@Mock
	EmployeeRepository employeeRepository;

    @Mock
	RoleRepository roleRepository;
     
    Employee employeeIn;

    Role roleIn;
    
    @Mock
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        employeeIn = new Employee();
        employeeIn.setId(5L);
        employeeIn.setName("TestUser");
        employeeIn.setSalary(1000);
        employeeIn.setDepartment("CSE");
        employeeIn.setPassword("1234");
        employeeIn.setRoles(new ArrayList<>());

        roleIn= new Role();
        roleIn.setId(5L);
        roleIn.setName("ROLE_TEST");
    }

    @Test
    void should_save_employee() { 

        String password = employeeIn.getPassword();
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeIn);
        when(passwordEncoder.encode(anyString())).thenReturn("%$%^%^^%^^%^%$%$%$&");
        Employee employeeOut = employeeServiceImpl.saveEmployee(employeeIn);

        verify(employeeRepository, times(1)).save(employeeIn);
        verify(passwordEncoder,times(1)).encode(password);
        assertNotNull(employeeOut);
        assertEquals("TestUser", employeeOut.getName());
    }

    @Test
    void should_get_employee() {
        long id = 5L;

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employeeIn));
        Employee employeeOut = employeeServiceImpl.getEmployee(id);

        verify(employeeRepository, times(1)).findById(id);
        assertNotNull(employeeOut);
        assertEquals("TestUser", employeeOut.getName());

    }

    @Test
    void should_get_employeeNotFound() {
        long id = 5L;

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employeeIn).empty());
        assertThrows(EmployeeServiceException.class, ()-> {employeeServiceImpl.getEmployee(id);});

    }

    @Test
    void should_save_role() {
    
        when(roleRepository.save(any())).thenReturn(roleIn);

        Role roleOut = employeeServiceImpl.saveRole(roleIn);

        verify(roleRepository, times(1)).save(roleIn);
        assertEquals("ROLE_TEST", roleOut.getName());
    }

    @Test
    void should_delete_employee() {
    
        long id = 5L;
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employeeIn));
        String result = employeeServiceImpl.deleteEmployee(id);
        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, times(1)).deleteById(id);
       
    }

    @Test
    void should_update_employee() {
    
        long id = 5L;
        String password = employeeIn.getPassword();
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employeeIn));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employeeIn);
        when(passwordEncoder.encode(anyString())).thenReturn("%$%^%^^%^^%^%$%$%$&");
        Employee employeeInAfterUpdate= new Employee();
        employeeIn.setId(5L);
        employeeIn.setName("TestUserUpdate");
        employeeIn.setSalary(1000);
        employeeIn.setDepartment("CSE");
        employeeIn.setPassword("1234");
        Employee employeeOutBefore = employeeServiceImpl.saveEmployee(employeeIn);
        Optional<Employee> employeeBeforeUpdate= employeeRepository.findById(employeeIn.getId());
        Employee employeeOutAfterUpdate=employeeServiceImpl.saveEmployee(employeeInAfterUpdate);
        verify(employeeRepository, times(1)).findById(employeeIn.getId());
        verify(employeeRepository, times(1)).save(employeeIn);
        verify(passwordEncoder,times(1)).encode(password);
        assertNotNull(employeeOutAfterUpdate);
        assertEquals("TestUserUpdate", employeeOutAfterUpdate.getName());
       
    }
     
    @AfterAll
    public static void cleanUp()
    {
        CacheManager.getInstance().shutdown();
    }
    
}
