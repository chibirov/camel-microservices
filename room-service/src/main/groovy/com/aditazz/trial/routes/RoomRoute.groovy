package com.aditazz.trial.routes

import java.util.Map
import com.aditazz.trial.dto.Room
import com.aditazz.trial.dto.RoomSearchResults
import org.springframework.stereotype.Component
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Value
import org.apache.camel.model.rest.RestBindingMode
import org.apache.camel.model.rest.RestParamType
import org.apache.camel.Exchange
import org.apache.camel.Processor

import org.apache.http.client.utils.URIBuilder

@Component
class RoomRoute extends RouteBuilder {
 
	@Value('${rest.host}') String host
	@Value('${rest.port}') String port

	@Override
	void configure() throws Exception {
		restConfiguration()
			.component('jetty')
			.host(host).port(port)
			.bindingMode(RestBindingMode.json)

		rest('/rooms')
			.post()
				.type(Room)
				.to('direct:createRoom')
			.get()
				.outType(RoomSearchResults)
				.to('direct:getRooms')
			.get('/{id}')
				.outType(Room)
				.to('direct:getRoom')
			.get('/{id}/content')
				.outType(Room)
				.to('direct:getRoomContent')				
			.delete('/{id}')
				.outType(Room)
				.to('direct:removeRoom')

		from('direct:createRoom')
			.to('jpa:com.aditazz.trial.dto.Room')

		from('direct:getRoomContent')
			.process(new Processor() {
                @Override
                public void process(Exchange ex) throws Exception {
                    exchange.setProperty("contentUrl", this.getContentUrl(exchange.getIn().getBody(Map.class)));
                }
            })
			.removeHeaders("*")
			.process(new Processor() {
                @Override
                public void process(Exchange ex) throws Exception {
                    exchange.getIn().setBody(null);
                }
            })
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.toD("${property.omdbUrl}")
			.process(new Processor() {
                @Override
                public void process(Exchange ex) throws Exception {
                    String xmlResponse = exchange.getIn().getBody(String.class);
					Map body = (Map) JaxbUtil.getInstance(Map.class, new ByteArrayInputStream(xmlResponse.getBytes()));
					exchange.getIn().setBody(body);
                }
            })
			.bean(converter, "convert(${body}")
			.end();

		from('direct:getRoom')
			.to('sql:select * from ROOM where id = :#${header.id}?dataSource=dataSource&outputType=SelectOne')
			.beanRef('transformer', 'mapRoom')

		from('direct:getRooms')
			.setProperty('query').method('transformer', 'constructQuery(${headers})')
			.toD('sql:${property.query}?dataSource=dataSource')
			.beanRef('transformer', 'mapRoomSearchResults')

		from('direct:removeRoom')
			.to('direct:getRoom')
			.setProperty('room', body())
			.to('sql:delete from ROOM where id = :#${body.id}?dataSource=dataSource')
			.setBody(property('room'))
	}
	
	String getContentUrl(Map params) {
		StringBuilder sb = new StringBuilder("http:/localhost:9090/contents/");
		if (params!=null) {
			if (!StringUtils.isEmpty(params.get("roomId"))) {
				sb.append("?room=");
				sb.append(params.get("roomId"));
			}
		}		
		try {
            URIBuilder uriBuilder = new URIBuilder(sb.toString());
            return uriBuilder.toString();
        } catch (URISyntaxException e) {
            LOGGER.error("Bad URL",e);
            return null;
        }
	}
}