package com.smartmanager.service;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.smartmanager.entities.EmailDetails;

@Service
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${spring.mail.username}")
	private String sender;

	//Method 1
	//To send simple email without attachment
	public boolean sendSimpleMail(EmailDetails emailDetails) {

		try {
			
			//Creating Simple mail message
			SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
			
			//setting up necessary details
			simpleMailMessage.setFrom(sender);
			simpleMailMessage.setTo(emailDetails.getRecipient());
			simpleMailMessage.setSubject(emailDetails.getSubject());
			simpleMailMessage.setText(emailDetails.getMsgBody());
			
			//Sending the mail
			javaMailSender.send(simpleMailMessage);
			
			return true;
			
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}
	
	
	//Method 2
	//to send email with attachment
	public boolean sendMailWithAttachment(EmailDetails emailDetails) {

		//Create the mime message
		MimeMessage mimeMessage =  javaMailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;
			
		try {
				
			//setting multipart as true for attachment
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			
			mimeMessageHelper.setFrom(sender);
			mimeMessageHelper.setTo(emailDetails.getRecipient());
			mimeMessageHelper.setSubject(emailDetails.getSubject());
			mimeMessageHelper.setText(emailDetails.getMsgBody());
			
			//Adding the attachment
			FileSystemResource fileSystemResource = new FileSystemResource(new File(emailDetails.getAttachment()));
			
			mimeMessageHelper.addAttachment(fileSystemResource.getFilename(), fileSystemResource);
			
			//Sending the file
			javaMailSender.send(mimeMessage);
			
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
		
		
	}

}
