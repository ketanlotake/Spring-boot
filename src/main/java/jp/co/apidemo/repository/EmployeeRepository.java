package jp.co.apidemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.co.apidemo.entities.Employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findByNameContaining(String name, Pageable pagingSort);
    Optional<Employee> findById(Long employeeId);
    Employee findByName(String name);
}
