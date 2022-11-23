## Spring boot features :
1. Authentication and JWT Authorization (End point protection)
2. Caching (EhCache)
3. Spring Data JPA : JPA, Paging and Sorting
4. Validation
5. Spring boot Annotations
6. Basic Exception Handling
7. Testing : UT and Functional test ( Due to shortage of time I have added selective test cases only)
8. Swagger API documentation
9. H2 Database 

## Requirements :
1. Postman
2. Maven : 3.8.6 
3. Java : 1.8

## Steps to Setup

**1. Clone the application**

```bash
git clone https://github.com/ketanlotake/java-challenge
```

**2. Build and run the app using maven**


```bash
1. mvn clean
2. mvn package
4. mvn spring-boot:run
```

The app will start running at <http://localhost:8080>.

## Application Details 

**1. Application Sequence Diagram**

![Optional Text](/img/Java_Challenge.png)

**2. Application Authentication**

To access the application use following authentication URL, add x-www-form-urlencoded key:value credentials data to request body on response it will provide access_token and refresh_token, use response access token to get authorization to access remaining URL as shown in sequence diagram.

**3. Application Autherization URL**

| Sr.NO  | API Function | Description |API URL | Input Parameter | Output |
| ------------- | ------------- | ------------- | ------------- |------------- |------------- |
| 1  | Authentication  | Login | POST http://localhost:8080/api/v1/login | Body (x-www-form-urlencoded) username:TESTMNG, password:1234| access_token, refresh_token|

**Point to remember** : access_token starts with "employee ", add string "employee " with once space before access token during authorization.

**4. Application API Documentation**

API documentation provided using swagger , please access following URL after running the application for API documentation.

- Swagger UI : http://localhost:8080/swagger-ui.html

**5. API access steps**
1. Get access_token value by executing authentication URL on Postman , ref-> Point 3.
![Optional Text](/img/Postman_Authentication.PNG)
**Suggestion** : Use Username:"TESTMNG" ,Password:"1234" as this employee has ROLE_MANAGER, permit with all operation.
2. Navigate to swagger UI, ref-> Point 4
3. Authorization : Swagger UI-> Authorize-> Add access_token (employee "access_token")
![Optional Text](/img/Swagger_authorization.PNG)
4. Access URL's.

**6. Role and Authorization Details**

| Role | SAVE EMPLOYEE/ROLE | RETRIVE |DELETE | UPDATE | 
| ------------- | ------------- | ------------- | ------------- |------------- |
| ROLE_MANAGER | YES  | YES | YES | YES |YES|
| ROLE_TEAM_LEADER | NO  | YES | YES|  NO |YES|
| ROLE_PROJECT_LEADER | NO  | YES | NO|  NO |NO|
| ROLE_ENGINEER | NO  | YES | NO | NO |NO|

**7. Database data**

Following data will be get added once spring application get run.

```bash
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
```
Database access link:

- H2 UI : http://localhost:8080/h2-console
