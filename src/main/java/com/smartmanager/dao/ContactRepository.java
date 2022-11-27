package com.smartmanager.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmanager.entities.Contact;
import com.smartmanager.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{

	//Pagination...
	
	@Query("FROM Contact c WHERE c.user.id = :userId")
	public Page<Contact> findContactByUserId(@Param("userId") int userId, Pageable page);
	
	
	//Search keywords
	public List<Contact> findByNameContainingAndUser(String name, User user);
}
