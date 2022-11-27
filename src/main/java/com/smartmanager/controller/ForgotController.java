package com.smartmanager.controller;

import java.security.Principal;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartmanager.dao.UserRepository;
import com.smartmanager.entities.EmailDetails;
import com.smartmanager.entities.User;
import com.smartmanager.helper.Message;
import com.smartmanager.service.EmailService;

@Controller
public class ForgotController {
	Random random = new Random(124568);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	//email id form open handler 
	@GetMapping("/forgot_password")
	public String openEmailForm() {
		
		
		return "forgot-password";
	}
	
	//Handler for send otp
	@PostMapping("/send_otp")
	public String sendOTP(@RequestParam("username") String username, HttpSession session) {

		System.out.println(username);
		int otp = random.nextInt(987689);
		
		System.out.println(otp);
		
		User user = this.userRepository.getUserByUserName(username);
		if(user == null) {
			session.setAttribute("message", new Message("User does not exist !!", "danger"));
			
			return "forgot-password";
		}
		
		//send otp to resp email
		String subject = "OTP from Smart Contact Manager";
		String message = " OTP = "+otp;
		String to = username;

		EmailDetails emailDetails = new EmailDetails(to, message, subject);
		
		boolean flag = true;
		//flag = this.emailService.sendSimpleMail(emailDetails);
		
		if(flag) {
			session.setAttribute("originalOTP", otp);
			session.setAttribute("username", username);
			session.setAttribute("message", new Message("OTP sent succesfully !!", "success"));
			return "verify-otp";
			
		}else {

			session.setAttribute("message", new Message("Something went wrong !!", "danger"));
			
			return "forgot-password";
		}
		
	}
	
	//Handler to verify otp
	@PostMapping("/verify_otp")
	public String verifyOTP(@RequestParam("otp") int enteredOTP, HttpSession session) {
		
		if(enteredOTP == (int)session.getAttribute("originalOTP")) {
			return "change-password";
		}else {

			session.setAttribute("message", new Message("You have entered wrong OTP !!", "danger"));			
			return "forgot-password";
		}
		
	}
	
	//Handler for change password
	@PostMapping("/update_password")
	public String changePassword(@RequestParam("newPassword") String newPassword, 
			@RequestParam("confirmNewPassword") String confirmNewPassword,
			HttpSession session) {
		
		if(!newPassword.equals(confirmNewPassword)) {
			session.setAttribute("message", new Message("New password not matched...!", "danger"));
			return "change-password";
		}

		User user = this.userRepository.getUserByUserName((String)session.getAttribute("username"));
		if(user == null) {
			session.setAttribute("message", new Message("User does not exist !!", "danger"));
			
			return "forgot-password";
		}
		
		//change password
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Password changed successfully...!", "success"));
		
		return "login";
	}
}
