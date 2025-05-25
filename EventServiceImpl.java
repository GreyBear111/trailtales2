package com.trailtales.service.impl;

import com.trailtales.dto.EventCreationDto;
import com.trailtales.entity.Event;
import com.trailtales.entity.Journey;
import com.trailtales.entity.Location;
import com.trailtales.entity.User;
import com.trailtales.repository.EventRepository;
import com.trailtales.repository.JourneyRepository;
import com.trailtales.repository.LocationRepository;
import com.trailtales.service.EventService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;
  private final JourneyRepository journeyRepository;
  private final LocationRepository locationRepository;
  private final Validator validator;

  @Autowired
  public EventServiceImpl(
      EventRepository eventRepository,
      JourneyRepository journeyRepository,
      LocationRepository locationRepository,
      Validator validator) {
    this.eventRepository = eventRepository;
    this.journeyRepository = journeyRepository;
    this.locationRepository = locationRepository;
    this.validator = validator;
  }

  @Override
  @Transactional
  public Event createEvent(EventCreationDto createDto, User currentUser) {
    Set<ConstraintViolation<EventCreationDto>> violations = validator.validate(createDto);
    if (!violations.isEmpty()) {
      throw new IllegalArgumentException(
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining("; ")));
    }

    if (createDto.getJourneyId() != null) {
      Journey journey =
          journeyRepository
              .findById(createDto.getJourneyId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Подорож з ID " + createDto.getJourneyId() + " не знайдено."));

      if (!journey.getUserId().equals(currentUser.getId())) {
        throw new SecurityException("Ви не маєте дозволу додавати події до цієї подорожі.");
      }
    }

    Location location = null;
    if (createDto.getLocationName() != null && !createDto.getLocationName().trim().isEmpty()) {
      location =
          locationRepository
              .findByName(createDto.getLocationName().trim())
              .orElseGet(
                  () -> {
                    Location newLocation = new Location();
                    newLocation.setName(createDto.getLocationName().trim());
                    newLocation.setDescription(
                        createDto.getLocationDescription() != null
                            ? createDto.getLocationDescription().trim()
                            : null);
                    return locationRepository.save(newLocation);
                  });
    }

    Event event = new Event();
    event.setName(createDto.getName());
    event.setDescription(createDto.getDescription());
    event.setEventDate(createDto.getEventDate());
    event.setEventTime(createDto.getEventTime());
    event.setJourneyId(createDto.getJourneyId());
    event.setLocationId(location != null ? location.getId() : null);
    event.setCreatedAt(LocalDateTime.now());
    event.setUpdatedAt(LocalDateTime.now());

    return eventRepository.save(event);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Event> getEventById(Long id) {
    return eventRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Event> getAllEvents() {
    return eventRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Event> getEventsByJourneyId(Long journeyId) {
    return eventRepository.findByJourneyId(journeyId);
  }

  @Override
  @Transactional
  public Event updateEvent(Event eventUpdates, User currentUser) {
    if (eventUpdates == null || eventUpdates.getId() == null) {
      throw new IllegalArgumentException(
          "Об'єкт події або його ID не може бути null для оновлення.");
    }

    Event existingEvent =
        eventRepository
            .findById(eventUpdates.getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати EventNotFoundException,
                        // якщо він є
                        "Подію з ID " + eventUpdates.getId() + " не знайдено для оновлення."));

    if (existingEvent.getJourneyId() != null) {
      Journey journey =
          journeyRepository
              .findById(existingEvent.getJourneyId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Пов'язана подорож для події з ID "
                              + existingEvent.getJourneyId()
                              + " не знайдена."));
      if (!journey.getUserId().equals(currentUser.getId())) {
        throw new SecurityException("Ви не маєте дозволу на оновлення цієї події.");
      }
    } else if (eventUpdates.getJourneyId() != null) {
      Journey journeyToLink =
          journeyRepository
              .findById(eventUpdates.getJourneyId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Подорож з ID "
                              + eventUpdates.getJourneyId()
                              + " для прив'язки не знайдена."));
      if (!journeyToLink.getUserId().equals(currentUser.getId())) {
        throw new SecurityException("Ви не маєте дозволу прив'язувати подію до цієї подорожі.");
      }
      existingEvent.setJourneyId(eventUpdates.getJourneyId());
    }

    Optional.ofNullable(eventUpdates.getName()).ifPresent(existingEvent::setName);
    Optional.ofNullable(eventUpdates.getDescription()).ifPresent(existingEvent::setDescription);
    Optional.ofNullable(eventUpdates.getEventDate()).ifPresent(existingEvent::setEventDate);
    Optional.ofNullable(eventUpdates.getEventTime()).ifPresent(existingEvent::setEventTime);

    if (eventUpdates.getLocationId() != null) {
      Location newLocation =
          locationRepository
              .findById(eventUpdates.getLocationId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException( // Можна використовувати
                          // LocationNotFoundException
                          "Локація з ID " + eventUpdates.getLocationId() + " не знайдена."));
      existingEvent.setLocationId(newLocation.getId());
    } else {
      existingEvent.setLocationId(null);
    }

    existingEvent.setUpdatedAt(LocalDateTime.now());
    return eventRepository.save(existingEvent);
  }

  @Override
  @Transactional
  public void deleteEvent(Long id, User currentUser) {
    Event eventToDelete =
        eventRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати EventNotFoundException
                        "Подія з ID " + id + " не знайдена для видалення."));

    if (eventToDelete.getJourneyId() != null) {
      Journey journey =
          journeyRepository
              .findById(eventToDelete.getJourneyId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException( // Можна використовувати
                          // JourneyNotFoundException
                          "Пов'язана подорож для події ID "
                              + eventToDelete.getJourneyId()
                              + " не знайдена."));
      if (!journey.getUserId().equals(currentUser.getId())) {
        throw new SecurityException("Ви не маєте дозволу на видалення цієї події.");
      }
    }
    eventRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Event> findByJourneyId(String journeyIdString) {
    try {
      Long journeyId = Long.parseLong(journeyIdString);
      return eventRepository.findByJourneyId(journeyId);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Некоректний формат ID подорожі: " + journeyIdString + ". Очікується числове значення.",
          e);
    }
  }
}
