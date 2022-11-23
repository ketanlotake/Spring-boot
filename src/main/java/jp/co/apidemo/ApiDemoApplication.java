package jp.co.apidemo;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jp.co.apidemo.entities.Employee;
import jp.co.apidemo.entities.Role;
import jp.co.apidemo.service.EmployeeService;

import org.springframework.cache.annotation.EnableCaching;
import java.util.ArrayList;


@SpringBootApplication
@EnableCaching
@EnableSwagger2
public class ApiDemoApplication {


	public static void main(String[] args) {
		SpringApplication.run(ApiDemoApplication.class, args);
	}

	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CommandLineRunner run(EmployeeService employeeService) {
		return args -> {
			employeeService.saveRole(new Role(null, "ROLE_ENGINEER"));
			employeeService.saveRole(new Role(null, "ROLE_PROJECT_LEADER"));
			employeeService.saveRole(new Role(null, "ROLE_TEAM_LEADER"));
			employeeService.saveRole(new Role(null, "ROLE_MANAGER"));

			employeeService.saveEmployee(new Employee(null, "TESTENGG", 1000, "DEVELOPMENT", "1234", new ArrayList<>()));
			employeeService.saveEmployee(new Employee(null, "TESTPL", 1000, "DEVELOPMENT", "1234", new ArrayList<>()));
			employeeService.saveEmployee(new Employee(null, "TESTTL", 1000, "DEVELOPMENT", "1234", new ArrayList<>()));
			employeeService.saveEmployee(new Employee(null, "TESTMNG", 1000, "DEVELOPMENT", "1234", new ArrayList<>()));

			employeeService.addRoleToEmployee("TESTENGG", "ROLE_ENGINEER");
			employeeService.addRoleToEmployee("TESTPL", "ROLE_PROJECT_LEADER");
			employeeService.addRoleToEmployee("TESTTL", "ROLE_TEAM_LEADER");
			employeeService.addRoleToEmployee("TESTMNG", "ROLE_MANAGER");

		};
	}

}
