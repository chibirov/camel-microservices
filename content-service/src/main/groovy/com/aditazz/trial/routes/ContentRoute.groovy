package com.aditazz.trial.routes

import com.aditazz.trial.dto.Content
import com.aditazz.trial.dto.ContentSearchResults
import org.springframework.stereotype.Component
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Value
import org.apache.camel.model.rest.RestBindingMode
import org.apache.camel.model.rest.RestParamType

@Component
class ContentRoute extends RouteBuilder {
 
	@Value('${rest.host}') String host
	@Value('${rest.port}') String port

	@Override
	void configure() throws Exception {
		restConfiguration()
			.component('jetty')
			.host(host).port(port)
			.bindingMode(RestBindingMode.json)

		rest('/contents')
			.post()
				.type(Content)
				.to('direct:createContent')
			.get()
				.outType(ContentSearchResults)
				.to('direct:getContents')
			.get('/{id}')
				.outType(Content)
				.to('direct:getContent')
			.get('?room={roomId}')
				.outType(ContentSearchResults)
				.to('direct:getContentsByRoom')				
			.delete('/{id}')
				.outType(Content)
				.to('direct:removeContent')

		from('direct:createContent')
			.to('jpa:com.aditazz.trial.dto.Content')

		from('direct:getContent')
			.to('sql:select * from Content where id = :#${header.id}?dataSource=dataSource&outputType=SelectOne')
			.beanRef('transformer', 'mapContent')

		from('direct:getContentsByRoom')
			.setProperty('query').method('transformer', 'constructQuery(${headers})')
			.to('sql:select * from Content where room_id = :#${header.id}?dataSource=dataSource')
			.beanRef('transformer', 'mapContentSearchResults')

		from('direct:getContents')
			.setProperty('query').method('transformer', 'constructQuery(${headers})')
			.toD('sql:${property.query}?dataSource=dataSource')
			.beanRef('transformer', 'mapContentSearchResults')

		from('direct:removeContent')
			.to('direct:getContent')
			.setProperty('Content', body())
			.to('sql:delete from Content where id = :#${body.id}?dataSource=dataSource')
			.setBody(property('Content'))
	}
}
