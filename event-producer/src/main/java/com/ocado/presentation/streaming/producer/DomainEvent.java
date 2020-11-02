package com.ocado.presentation.streaming.producer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DomainEvent {

	private final String eventType;
	private final String id;

	@JsonCreator
	public DomainEvent(@JsonProperty("eventType") String eventType, @JsonProperty("id") String id) {
		this.eventType = eventType;
		this.id = id;
	}

	public String getEventType() {
		return eventType;
	}

	public String getId() {
		return id;
	}

}
