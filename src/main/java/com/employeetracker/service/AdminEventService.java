package com.employeetracker.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AdminEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        return emitter;
    }

    public void broadcastStatusChange(Long userId, String status) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("status-change")
                        .data("{\"userId\":" + userId + ",\"status\":\"" + status + "\"}"));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
    public void broadcastGeofenceEvent(Long userId, String employeeName, String geofenceName, String geofenceType, String eventType) {
        List<SseEmitter> dead = new CopyOnWriteArrayList<>();
        String label = eventType.equals("ENTER") ? "Entered" : ("OFFICE".equals(geofenceType) ? "Exited" : "Left");
        String payload = String.format(
                "{\"userId\":%d,\"employeeName\":\"%s\",\"geofenceName\":\"%s\",\"geofenceType\":\"%s\",\"eventType\":\"%s\",\"message\":\"%s %s %s\"}",
                userId, employeeName, geofenceName, geofenceType, eventType, employeeName, label, geofenceName);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("geofence-event").data(payload));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}