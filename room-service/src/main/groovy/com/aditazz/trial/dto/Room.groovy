package com.aditazz.trial.dto

import javax.persistence.*

@Entity(name = "ROOM")
class Room {
	@Id @GeneratedValue @Column(name = "ID") Integer id
	@Column(name = "SIZE") Double size
}