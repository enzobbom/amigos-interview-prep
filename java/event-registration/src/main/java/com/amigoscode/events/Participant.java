package com.amigoscode.events;

import java.util.Objects;

public class Participant {
    private String eventId;
    private String participantId;
    private String email;
    private String skillLevel;

    public Participant(String eventId, String participantId, String email, String skillLevel) {
        this.eventId = eventId;
        this.participantId = participantId;
        this.email = email;
        this.skillLevel = skillLevel;
    }

    public String getEventId() {
        return eventId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public String getEmail() {
        return email;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(eventId, that.eventId) && Objects.equals(participantId, that.participantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, participantId);
    }
}
