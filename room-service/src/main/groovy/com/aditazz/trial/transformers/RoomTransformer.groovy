package com.aditazz.trial.transformers

import com.aditazz.trial.dto.Room
import com.aditazz.trial.dto.RoomSearchResults
import org.springframework.stereotype.Component
import java.util.Map
import java.util.List

@Component('transformer')
class RoomTransformer {
	Room mapRoom(Map map) {
		new Room(id: map.id, size: map.size, content: map.content)
	}

	String constructQuery(Map headers) {
		def wheres = []
		if (headers.name) {
			wheres << 'name = :#${header.size}'
		}

		def query = 'select * from ROOM'
		if (wheres) {
			query += ' where ' + wheres.join(' and ')
		}
		return query
	}

	RoomSearchResults mapRoomSearchResults(List<Map> body) {
		new RoomSearchResults(
			size: body.size,
			rooms: body.collect { mapRoom it }
		)
	}
}