package com.smartmanager.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.websocket.Session;

import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartmanager.dao.UserRepository;
import com.smartmanager.entities.User;
import com.smartmanager.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@GetMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	//Handler for registering user
	@PostMapping("/do_register")
	public String registerUser(

			@Valid @ModelAttribute("user") User user, BindingResult result, 
			@RequestParam(value = "agreement", 
			defaultValue = "false") boolean agreement, 
			Model model, 
			HttpSession session
			
			) {
		
		try {

			if(result.hasErrors()) {
				System.out.println("Error = "+result.toString());
				model.addAttribute("user", user);
				return "signup";
			} 
			
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			
			System.out.println("Agreement = "+agreement);
			System.out.println("USER = "+user);
			
			User resultUser = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success")); 
			return "signup";
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message(e.getLocalizedMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	
	//Handler for custom login
	
	@GetMapping("/login")
	public String customLogin(Model model) {

		model.addAttribute("title", "Login - Smart Contact Manager");
		
		return "login";
	}
}



















