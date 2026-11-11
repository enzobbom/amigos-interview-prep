package com.amigoscode.events;

import java.util.*;

public class Event {
    private final String eventId;
    private final String eventType;
    private final int capacity;
    private final String requirements;
    private final Map<String, Object> parsedRequirements;

    private final Set<Participant> participants = new HashSet<>();
    private final Deque<Participant> waitList = new ArrayDeque<>();

    private static final String SKILL_LEVEL_REQUIREMENT_NAME = "skill_level";
    private static final String BUSINESS_EMAIL_REQUIREMENT_NAME = "business_email";
    private static final List<String> VALID_SKILL_LEVELS = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
    private static final String SKILL_LEVEL_REQUIREMENT_WILDCARD = "any";
    private static final List<String> INVALID_BUSINESS_EMAIL_DOMAINS = List.of("gmail.com", "outlook.com", "yahoo.com", "hotmail.com");

    private Event(String eventId, String eventType, int capacity, String requirements, Map<String, Object> parsedRequirements) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.capacity = capacity;
        this.requirements = requirements;
        this.parsedRequirements = parsedRequirements;
    }

    public static Optional<Event> create(String eventId, String eventType, int capacity, String requirements) {
        Map<String, Object> parsedRequirements;

        try {
            parsedRequirements = parseRequirements(requirements);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        if (eventId.isBlank() || eventType.isBlank() || capacity <= 0) {
            return Optional.empty();
        }

        return Optional.of(new Event(
                eventId,
                eventType,
                capacity,
                requirements,
                parsedRequirements
        ));
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getRequirements() {
        return requirements;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(eventId, event.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    // participant management

    public int addParticipant(Participant participant) {
        if (participants.contains(participant) ||
                waitList.contains(participant) ||
                !isParticipantAllowed(participant.getEmail(), participant.getSkillLevel())) {

            return -1;
        }

        if (participants.size() >= capacity) {
            waitList.add(participant);
            return waitList.size();
        }

        return 0;
    }

    public boolean removeParticipant(String participantId) {
        return true;
    }

    // internal methods

    public boolean isParticipantAllowed(String participantEmail, String participantSkillLevel) {
        if ((boolean) parsedRequirements.get(BUSINESS_EMAIL_REQUIREMENT_NAME)) {
            for (String e : INVALID_BUSINESS_EMAIL_DOMAINS) {
                if (participantEmail.contains(e)) {
                    return false;
                }
            }
        }

        String eventSkillLevel = (String) parsedRequirements.get(SKILL_LEVEL_REQUIREMENT_NAME);
        if (eventSkillLevel.equals(SKILL_LEVEL_REQUIREMENT_WILDCARD)) {
            return true;
        }

        return participantSkillLevel.equals(eventSkillLevel);
    }

    private static Map<String, Object> parseRequirements(String requirements) {

        /*
        Basic validation of full requirement string:
            - not null;
            - if blank -> any valid email accepted, any skill level accepted
         */

        if (requirements == null) {
            throw new IllegalArgumentException("Requirements cannot be null");
        }

        Map<String, Object> parsedRequirements = new HashMap<>();

        if (requirements.trim().isBlank()) {
            parsedRequirements.put(BUSINESS_EMAIL_REQUIREMENT_NAME, false);
            parsedRequirements.put(SKILL_LEVEL_REQUIREMENT_NAME, SKILL_LEVEL_REQUIREMENT_WILDCARD);

            return parsedRequirements;
        }

        // parse the parsedRequirements: valid fields, pairs and no duplicates

        String[] splitRequirements = requirements.split(",", -1); // doesn't discard trailing spaces
        String key;
        Object value;
        for (String requirement : splitRequirements) {
            requirement = requirement.trim();

            if (requirement.isEmpty()) { // empty requirement between commas
                throw new IllegalArgumentException("Requirement cannot be empty");
            }

            int colonIdx = requirement.indexOf(":");

            if (colonIdx == -1) { // no ":"
                key = requirement;
                value = null;

            } else if (colonIdx != requirement.lastIndexOf(":")) { // more than one ":"
                throw new IllegalArgumentException("Key-value parsedRequirements must have format key:value");

            } else { // single ":"
                String[] splitRequirement = requirement.split(":", -1);

                if (splitRequirement[0].trim().isEmpty() || splitRequirement[1].trim().isEmpty()) {
                    throw new IllegalArgumentException("Key-value parsedRequirements must have a non-empty key and value");
                }

                key = splitRequirement[0].trim();
                value = splitRequirement[1].trim();
            }

            if (parsedRequirements.containsKey(key)) {
                throw new IllegalArgumentException("Requirements cannot be repeated");
            }

            // validate the actual keys and values

            if (key.equals(BUSINESS_EMAIL_REQUIREMENT_NAME)) {
                value = true;

            } else if (!key.equals(SKILL_LEVEL_REQUIREMENT_NAME)) {
                throw new IllegalArgumentException("Unknown requirement");

            } else if (!VALID_SKILL_LEVELS.contains(value)) {
                throw new IllegalArgumentException("Invalid skill level");
            }

            parsedRequirements.put(key, value);
        }

        return parsedRequirements;
    }
}
