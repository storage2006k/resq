package com.aris.websocket;

import com.aris.model.EventLog;
import com.aris.repository.EventLogRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final EventLogRepository eventLogRepository;

    public EventBroadcaster(SimpMessagingTemplate messagingTemplate,
                            EventLogRepository eventLogRepository) {
        this.messagingTemplate = messagingTemplate;
        this.eventLogRepository = eventLogRepository;
    }

    public void broadcast(String source, String message, String type) {
        EventLog event = new EventLog(source, message, type);
        eventLogRepository.save(event);
        messagingTemplate.convertAndSend("/topic/events", event);
    }
}
