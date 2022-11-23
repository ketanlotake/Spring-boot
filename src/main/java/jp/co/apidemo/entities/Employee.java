package jp.co.apidemo.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import static javax.persistence.FetchType.*;



@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Builder

@Entity 
public class Employee implements Serializable{
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @Column(name="EMPLOYEE_NAME", nullable = false, unique = true, length = 100)
    @Pattern(regexp="^[A-Za-z]*$",message = "Employee name must contain only letters")
    private String name;


    @Column(name="EMPLOYEE_SALARY", nullable = false)
    private Integer salary;

    
    @Column(name="DEPARTMENT")
    @Pattern(regexp="^[A-Za-z]*$",message = "Department name must contain only letters")
    private String department;

    
    @Column(name="PASSWORD")
    private String password;

    @Column(name="ROLES")
    @ManyToMany(fetch = EAGER)
    @JoinTable(name ="employee_roles",
    joinColumns = @JoinColumn(name = "employee_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles = new ArrayList<>();

    
}