package com.aditazz.trial.dto

import javax.persistence.*

@Entity(name = "CONTENT")
class Content {
	@Id @GeneratedValue @Column(name = "ID") Integer id
	@Column(name = "SIZE") Double size
	@Column(name = "ROOM_ID") Integer roomId	
}