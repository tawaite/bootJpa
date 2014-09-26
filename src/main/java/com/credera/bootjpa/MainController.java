package com.credera.bootjpa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@ComponentScan
@EnableAutoConfiguration
@Configuration
public class MainController {
	
	@Autowired
	private static ApplicationContext context;
	
	@RequestMapping("/")
	@ResponseBody
	public byte[] home() throws JsonGenerationException, JsonMappingException, IOException {
		CustomerRepository repository = context.getBean(CustomerRepository.class);
		Iterable<Customer> customers = repository.findAll();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final ObjectMapper mapper = new ObjectMapper();
		
		mapper.writeValue(out, customers);
		
		final byte[] data = out.toByteArray();
		
		return data;
	}
	
	@RequestMapping(value = "/addName", method = RequestMethod.POST)
	@ResponseBody
	public String addName(@RequestParam("first") String first, @RequestParam("last") String last,
			@RequestParam("token") String token) {	
		
		System.out.println("Name: " + first + " " + last + " " + token);
		
		try {
			System.out.println("try");
			//Will throw an exception if token is invalid
			//If you want to check the expiration this could be done by saving a TokenInfo object
			//Then checking its time against the expires date.
			TokenService.verifyToken(token);
			context.getBean((CustomerRepository.class)).save(new Customer(first, last));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return "test";
		
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	@ResponseBody
	public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
		System.out.println("User credentials: " + username + ", " + password);
		
		//See if the user exists in the database
		if (userExists(username)) {
			//Check if the password matches
			if (validPassword(username, password)) {
				String jwt = TokenService.createJsonWebToken(username, (long) 1);
				return jwt;
			//If the password didn't match tell the client
			} else {
				return "Password invalid";
			}
		}
		//This will be returned if the user does not exist in the db
		return "DNE";
	}
	
	//This will register new users to database
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public String login(@RequestParam("email") String email, @RequestParam("password") String password, 
			@RequestParam("admin") boolean admin) {
		
		System.out.println(email + ", " + password + ", " + admin);
		
		//Get the db
		UserRepository repository = context.getBean(UserRepository.class);
		
		//Make sure the user doesn't already exist
		if ( userExists(email) ) {
			//Return that the user already exists
			return "Email already exists";
		//Otherwise save the new user to the database
		} else {
			context.getBean(UserRepository.class).save(new User(email, password, admin));
			return "Login";
		}
	}
	
	//Check if the user exists
	public boolean userExists(String email) {
		
		//Get the db
		UserRepository repository = context.getBean(UserRepository.class);
		
		//Checks if the user exists in the db
		if ( repository.findByEmail(email).size() > 0 ) {
			System.out.println("User exists!");
			return true;
			
		//If not return false
		} else {
			System.out.println("User dne");
			return false;
		}
	}
	
	//See if the password matches the user !careful this doesn't make sure the user exists!
	public boolean validPassword(String email, String password) {
		
		//Get the db
		UserRepository repository = context.getBean(UserRepository.class);
		//Get the user from the db
		User user = repository.findByEmail(email).get(0);
		
		//Verify the password
		if ( user.getPassword().equals(password) ) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public static void main(String[] args) {
		context = SpringApplication.run(MainController.class, args);
		CustomerRepository repository = context.getBean(CustomerRepository.class);
		UserRepository users = context.getBean(UserRepository.class);
		
		//Save some customers
		repository.save(new Customer("Jack", "Bauer"));
        repository.save(new Customer("Chloe", "O'Brian"));
        repository.save(new Customer("Kim", "Bauer"));
        repository.save(new Customer("David", "Palmer"));
        repository.save(new Customer("Michelle", "Dessler"));
        
        //Save some admins
        users.save(new User("admin@credera.com", "password", true));
        users.save(new User("twaite@credera.com", "password", true));
        
	}

}
