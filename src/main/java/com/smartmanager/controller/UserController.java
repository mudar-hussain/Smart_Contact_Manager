package com.smartmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smartmanager.dao.ContactRepository;
import com.smartmanager.dao.UserRepository;
import com.smartmanager.entities.Contact;
import com.smartmanager.entities.User;
import com.smartmanager.helper.Message;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		//System.out.println("Username = "+userName);
		
		//get user using userName
		
		User user = userRepository.getUserByUserName(userName);
		//System.out.println(user.toString());
		
		model.addAttribute("user", user);
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model, Principal principal) {
		
		
		return "normal/user-dashboard";
	}
	
	//Open create contact
	@GetMapping("/add_contact")
	public String createContact(Model model) {
		
		model.addAttribute("title", "Create Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add-contact";
	}
	
	//processing create contact
	@PostMapping("/process_contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) 
	{
		try {
			String name= principal.getName();
			User user = userRepository.getUserByUserName(name);
			//Processing and uploading file
			
			if(file.isEmpty()) {
				//write error message or whatever you want
				
				System.out.println("File is empty");
				contact.setImageUrl("default.png");	
			}else {
				//save file to folder and update image url
				contact.setImageUrl(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img/profile").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
			System.out.println(contact);
			
			//Success message...................
			session.setAttribute("message", new Message("New contact created succesfully !!", "success"));
			
			
			
		}catch (Exception e) {
			e.printStackTrace();
			//Error message..................
			session.setAttribute("message", new Message("Something went wrong. Please try again !!", "danger"));
			
			
			
		}
		return "normal/add-contact";
	}
	

	//Open Update contact
	@GetMapping("/{cId}/update_contact")
	public String updateContact(@PathVariable("cId") int cId ,Model model, Principal principal) {
	
		model.addAttribute("title", "Update Contact");
		Contact contact = contactRepository.findById(cId).get();
				
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
		}
		
		
		return "normal/update-contact";
	}
	
	//Handler for Update Contact
	@PostMapping("/process_update")
	public String UpdateContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session) {
		

		System.out.println(contact);
		
		try {
			String name= principal.getName();
			User user = userRepository.getUserByUserName(name);
			Contact oldContact = contactRepository.findById(contact.cId).get();
			
			//Processing and uploading file
			if(file.isEmpty()) {
				//write error message or whatever you want
				
				contact.setImageUrl(oldContact.getImageUrl());	
			}else {
				if(contact.getImageUrl() != "default.png") {
					//delete old profile pic
	
					File deleteFile = new ClassPathResource("static/img/profile").getFile();
					deleteFile = new File(deleteFile, oldContact.getImageUrl());
					deleteFile.delete();
					
				}
				
				
				
				//save file to folder and update image url
				contact.setImageUrl(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img/profile").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			System.out.println(contact);
			
			//Success message...................
			session.setAttribute("message", new Message("Contact updated succesfully !!", "success"));
			
		}catch (Exception e) {
			e.printStackTrace();
			//Error message..................
			session.setAttribute("message", new Message("Something went wrong. Please try again !!", "danger"));
			
		}
		
		return "normal/update-contact";
	}
	
	//Handler for Show Contacts
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") int page, Model model, Principal principal) {
		model.addAttribute("title", "Contacts");
		
		//Create contacts list
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		//currentPage-page
		//Contact per page-5
		Pageable  pageable = PageRequest.of(page, 5);
		
		Page<Contact> listContacts = contactRepository.findContactByUserId(user.getId(), pageable);
		
		model.addAttribute("contacts", listContacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", listContacts.getTotalPages());
		return "normal/show-contacts";
	}
	
	//Handler for showing particular Contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") int cId, Model model, Principal principal) {
		
		//Contact contact = contactRepository.getById(id);
		Optional<Contact> optionalContact = contactRepository.findById(cId);
		Contact contact = optionalContact.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact-detail";
	}
	
	//Handler for deleting contact
	@GetMapping("/{cId}/delete_contact")
	public String deleteContact(@PathVariable("cId") int cId, Model model, Principal principal, HttpSession session) {
		try {
			Contact contact = contactRepository.findById(cId).get();
			
			User user = this.userRepository.getUserByUserName(principal.getName());
				
			if(user.getId() == contact.getUser().getId()) {
	
				model.addAttribute("title", "Contacts");

				user.getContacts().remove(contact);
				
				this.userRepository.save(user);
				
				
				session.setAttribute("message", new Message("Contact deleted successfully...!", "success"));
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return "redirect:/user/show_contacts/0";
	}
	
	//Handler for Profile Page
	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		model.addAttribute("title", user.getName());
		return "normal/profile";
	}
	
	//Handler for Settings
	
	@GetMapping("/settings")
	public String settings(Model model, Principal principal) {
		model.addAttribute("title", "Settings");
		
		
		return "normal/settings";
	}
	
	//Handler for Change password
	
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("currentPassword") String currentPassword, 
			@RequestParam("newPassword") String newPassword, 
			@RequestParam("confirmNewPassword") String confirmNewPassword,
			Principal principal,
			HttpSession session) {
		
		System.out.println(currentPassword);
		User user = this.userRepository.getUserByUserName(principal.getName());
		try {
			if(newPassword != confirmNewPassword) {
				session.setAttribute("message", new Message("New password not matched...!", "danger"));
				return "normal/settings";
			}
			if(this.bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
				//change password
				user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
				this.userRepository.save(user);
				
				session.setAttribute("message", new Message("Password changed successfully...!", "success"));
				return "normal/user-dashboard";
				
			}else {
				//error
	
				session.setAttribute("message", new Message("Please enter correct current password...!", "danger"));
				
				return "normal/settings";
			}
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "normal/settings";
		}
		
	}
}


 




























