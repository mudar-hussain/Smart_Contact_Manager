package com.smartmanager.service;

import com.smartmanager.entities.EmailDetails;

public interface EmailService {

	//to send simple email
	boolean sendSimpleMail(EmailDetails emailDetails);
	
	//to send mail with attachment
	boolean sendMailWithAttachment(EmailDetails emailDetails);
}
