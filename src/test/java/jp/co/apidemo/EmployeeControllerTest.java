package jp.co.apidemo;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.co.apidemo.entities.Employee;
import jp.co.apidemo.entities.Role;
import jp.co.apidemo.service.EmployeeService;

import org.json.simple.JSONObject;  
import org.json.simple.JSONValue;  
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import net.sf.ehcache.CacheManager;

import javax.swing.text.PasswordView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableCaching
@Slf4j
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    EmployeeService employeeService;

    @Autowired
    PasswordEncoder passwordEncoder;
    

    private final String loginUser = "TESTMNG";
    private final String loginPassword = "1234";
    
    private  HttpHeaders header = new HttpHeaders();

     private final Employee empIn = new Employee(5L,
        "DummyTestEmployee",
        1000,
        "DEVELOPMENT",
        loginPassword,
        new ArrayList<>()
    );

    private final Role roleIn = new Role(5L,
        "ROLE_TEST"
    );
 

    @Test
    public void test_Save_Employee() throws Exception {
       
        String token = obtainAccessToken(loginUser, loginPassword);
        System.out.println("employee " + token);
        HttpHeaders header = new HttpHeaders();
        header.add(AUTHORIZATION, "employee " + token);
        String requestJson=objToJsonString(empIn);

        String result = mockMvc
            .perform(post("/api/v1/employee/save")
                .headers(header)
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Object obj=JSONValue.parse(result);  
        JSONObject jsonObject = (JSONObject) obj;  
      
        String name = (String) jsonObject.get("name");  
        System.out.println("Employee "+ name);
        assertEquals(empIn.getName(), name); 
   
    }

    @Test
    public void test_Save_Role() throws Exception {
      
        String token = obtainAccessToken(loginUser, loginPassword);
        System.out.println("employee " + token);
        HttpHeaders header = new HttpHeaders();
        header.add(AUTHORIZATION, "employee " + token);
        String requestJson=objToJsonString(roleIn);

        String result = mockMvc
            .perform(post("/api/v1/role/save")
                .headers(header)
                .contentType(MediaType.APPLICATION_JSON).content(requestJson))
            .andExpect(status().is2xxSuccessful())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Object obj=JSONValue.parse(result);  
        JSONObject jsonObject = (JSONObject) obj;  
      
        String name = (String) jsonObject.get("name");  
        System.out.println("Role name "+ name);
        assertEquals(roleIn.getName(),name); 
   
    }


   @Test
   public void testGetEmployeeUsingId() throws Exception {
        
        Long id=1L;

        String token = obtainAccessToken(loginUser, loginPassword);
        System.out.println("employee " + token);
        HttpHeaders header = new HttpHeaders();
        header.add(AUTHORIZATION, "employee " + token);

        String result = mockMvc
            .perform(get("/api/v1/employee/get/" + id)
                .headers(header)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

            Object obj=JSONValue.parse(result);  
            JSONObject jsonObject = (JSONObject) obj;  
          
            String name = (String) jsonObject.get("name");  
            System.out.println("Employee using id"+ name);
            assertEquals("TESTENGG", name); 
    } 

    private String obtainAccessToken(String username, String password) throws Exception {

        ResultActions result = mockMvc.perform(
            post("/api/v1/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("username", username)
                .param("password", password)
            ).andExpect(status().isOk());

        String resultString = result.andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("access_token").toString();
    }

    private String objToJsonString(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    } 

    @AfterAll
    public static void cleanUp()
    {
        CacheManager.getInstance().shutdown();
    }
    
    

}
