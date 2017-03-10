package com.aditazz.trial.transformers

import com.aditazz.trial.dto.Content
import com.aditazz.trial.dto.ContentSearchResults
import org.springframework.stereotype.Component
import java.util.Map
import java.util.List

@Component('transformer')
class ContentTransformer {
	Content mapContent(Map map) {
		new Content(id: map.id, size: map.size, rooms: map.rooms)
	}

	String constructQuery(Map headers) {
		def wheres = []
		if (headers.name) {
			wheres << 'name = :#${header.size}'
		}

		def query = 'select * from CONTENT'
		if (wheres) {
			query += ' where ' + wheres.join(' and ')
		}
		return query
	}

	ContentSearchResults mapContentSearchResults(List<Map> body) {
		new ContentSearchResults(
			size: body.size,
			contents: body.collect { mapContent it }
		)
	}
}