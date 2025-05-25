package com.trailtales.service.impl;

import com.trailtales.dto.JourneyCreationDto;
import com.trailtales.dto.JourneyUpdateDto;
import com.trailtales.entity.Journey;
import com.trailtales.entity.Location;
import com.trailtales.entity.Tag;
import com.trailtales.entity.User;
import com.trailtales.repository.JourneyRepository;
import com.trailtales.repository.LocationRepository;
import com.trailtales.repository.TagRepository;
import com.trailtales.repository.UserRepository;
import com.trailtales.service.JourneyService;
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
public class JourneyServiceImpl implements JourneyService {

  private final JourneyRepository journeyRepository;
  private final TagRepository tagRepository;
  private final LocationRepository locationRepository;
  private final UserRepository userRepository;
  private final Validator validator;

  /**
   * Конструктор для впровадження залежностей.
   *
   * @param journeyRepository Репозиторій для роботи з подорожами.
   * @param tagRepository Репозиторій для роботи з тегами.
   * @param locationRepository Репозиторій для роботи з локаціями.
   * @param userRepository Репозиторій для роботи з користувачами.
   * @param validator Валідатор для перевірки DTO.
   */
  @Autowired
  public JourneyServiceImpl(
      JourneyRepository journeyRepository,
      TagRepository tagRepository,
      LocationRepository locationRepository,
      UserRepository userRepository,
      Validator validator) {
    this.journeyRepository = journeyRepository;
    this.tagRepository = tagRepository;
    this.locationRepository = locationRepository;
    this.userRepository = userRepository;
    this.validator = validator;
  }

  /**
   * Створює нову подорож на основі JourneyCreationDto. Виконує валідацію DTO та встановлює власника
   * подорожі.
   *
   * @param createDto Об'єкт JourneyCreationDto, що містить дані для створення подорожі.
   * @param currentUser Поточний автентифікований користувач.
   * @return Створений об'єкт Journey.
   * @throws IllegalArgumentException якщо DTO недійсний.
   */
  @Override
  @Transactional
  public Journey createJourney(JourneyCreationDto createDto, User currentUser) {
    Set<ConstraintViolation<JourneyCreationDto>> violations = validator.validate(createDto);
    if (!violations.isEmpty()) {
      throw new IllegalArgumentException(
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining("; ")));
    }

    Journey journey = new Journey();
    journey.setUserId(currentUser.getId());
    journey.setName(createDto.getName());
    journey.setDescription(createDto.getDescription());
    journey.setStartDate(createDto.getStartDate());
    journey.setEndDate(createDto.getEndDate());
    journey.setCreatedAt(LocalDateTime.now());
    journey.setUpdatedAt(LocalDateTime.now());

    // Обробка початкової локації
    if (createDto.getOriginLocationName() != null && !createDto.getOriginLocationName().isEmpty()) {
      Location originLocation =
          locationRepository
              .findByName(createDto.getOriginLocationName())
              .orElseGet(
                  () -> {
                    Location newLocation = new Location();
                    newLocation.setName(createDto.getOriginLocationName());
                    // Виправлено: перевіряємо наявність getOriginLocationDescription()
                    newLocation.setDescription(
                        createDto.getOriginLocationDescription() != null
                            ? createDto.getOriginLocationDescription()
                            : null);
                    return locationRepository.save(newLocation);
                  });
      journey.setOriginLocationId(originLocation.getId());
      journey.setOriginLocation(originLocation);
    }

    // Обробка кінцевої локації
    if (createDto.getDestinationLocationName() != null
        && !createDto.getDestinationLocationName().isEmpty()) {
      Location destinationLocation =
          locationRepository
              .findByName(createDto.getDestinationLocationName())
              .orElseGet(
                  () -> {
                    Location newLocation = new Location();
                    newLocation.setName(createDto.getDestinationLocationName());
                    // Виправлено: перевіряємо наявність getDestinationLocationDescription()
                    newLocation.setDescription(
                        createDto.getDestinationLocationDescription() != null
                            ? createDto.getDestinationLocationDescription()
                            : null);
                    return locationRepository.save(newLocation);
                  });
      journey.setDestinationLocationId(destinationLocation.getId());
      journey.setDestinationLocation(destinationLocation);
    }

    // Обробка тегів (використовуємо getTagNames() замість getTags())
    if (createDto.getTagNames() != null && !createDto.getTagNames().isEmpty()) {
      Set<Tag> tags =
          createDto.getTagNames().stream() // Виправлено: getTagNames()
              .map(
                  tagName ->
                      tagRepository
                          .findByName(tagName)
                          .orElseGet(
                              () -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                return tagRepository.save(newTag);
                              }))
              .collect(Collectors.toSet());
      journey.setTags(tags);
    }

    // Збереження подорожі
    return journeyRepository.save(journey);
  }

  /**
   * Повертає подорож за її ID.
   *
   * @param id ID подорожі.
   * @return Optional, що містить Journey, якщо знайдено, або порожній Optional.
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<Journey> getJourneyById(Long id) {
    return journeyRepository.findById(id);
  }

  /**
   * Повертає список усіх подорожей.
   *
   * @return Список об'єктів Journey.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Journey> getAllJourneys() {
    return journeyRepository.findAll();
  }

  /**
   * Повертає список подорожей, створених певним користувачем.
   *
   * @param userId ID користувача.
   * @return Список об'єктів Journey.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Journey> getJourneysByUserId(Long userId) {
    return journeyRepository.findByUserId(userId);
  }

  /**
   * Додає тег до подорожі.
   *
   * @param journeyId ID подорожі.
   * @param tagName Назва тегу.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey addTagToJourney(Long journeyId, String tagName, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);

    Tag tag =
        tagRepository
            .findByName(tagName)
            .orElseGet(
                () -> {
                  Tag newTag = new Tag();
                  newTag.setName(tagName);
                  return tagRepository.save(newTag);
                });

    journey.getTags().add(tag);
    return journeyRepository.save(journey);
  }

  /**
   * Видаляє тег з подорожі.
   *
   * @param journeyId ID подорожі.
   * @param tagName Назва тегу.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey removeTagFromJourney(Long journeyId, String tagName, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);

    Optional<Tag> tagOpt = tagRepository.findByName(tagName);
    if (tagOpt.isPresent()) {
      journey.getTags().removeIf(t -> t.getName().equals(tagName));
      return journeyRepository.save(journey);
    } else {
      throw new IllegalArgumentException("Тег з назвою '" + tagName + "' не знайдено.");
    }
  }

  /**
   * Повертає теги для певної подорожі.
   *
   * @param journeyId ID подорожі.
   * @return Набір об'єктів Tag.
   */
  @Override
  @Transactional(readOnly = true)
  public Set<Tag> getTagsForJourney(Long journeyId) {
    return tagRepository.findTagsByJourneyId(
        journeyId); // Виправлено: викликаємо findTagsByJourneyId
  }

  /**
   * Встановлює початкову локацію для подорожі.
   *
   * @param journeyId ID подорожі.
   * @param locationName Назва початкової локації.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey setOriginLocationForJourney(
      Long journeyId,
      String locationName,
      String locationDescription,
      User currentUser) { // ДОДАНО locationDescription
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);
    Location location =
        locationRepository
            .findByName(locationName.trim())
            .orElseGet(
                () -> {
                  Location newLocation = new Location();
                  newLocation.setName(locationName.trim());
                  newLocation.setDescription(
                      locationDescription != null
                          ? locationDescription.trim()
                          : null); // ВИКОРИСТОВУЄМО locationDescription
                  return locationRepository.save(newLocation);
                });
    journey.setOriginLocationId(location.getId());
    journey.setOriginLocation(location);
    journey.setUpdatedAt(LocalDateTime.now());
    return journeyRepository.save(journey);
  }

  /**
   * Встановлює кінцеву локацію для подорожі.
   *
   * @param journeyId ID подорожі.
   * @param locationName Назва кінцевої локації.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey setDestinationLocationForJourney(
      Long journeyId,
      String locationName,
      String locationDescription,
      User currentUser) { // ДОДАНО locationDescription
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);
    Location location =
        locationRepository
            .findByName(locationName.trim())
            .orElseGet(
                () -> {
                  Location newLocation = new Location();
                  newLocation.setName(locationName.trim());
                  newLocation.setDescription(
                      locationDescription != null
                          ? locationDescription.trim()
                          : null); // ВИКОРИСТОВУЄМО locationDescription
                  return locationRepository.save(newLocation);
                });
    journey.setDestinationLocationId(location.getId());
    journey.setDestinationLocation(location);
    journey.setUpdatedAt(LocalDateTime.now());
    return journeyRepository.save(journey);
  }

  /**
   * Видаляє початкову локацію з подорожі.
   *
   * @param journeyId ID подорожі.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey removeOriginLocationForJourney(Long journeyId, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);
    journey.setOriginLocationId(null);
    journey.setOriginLocation(null);
    return journeyRepository.save(journey);
  }

  /**
   * Видаляє кінцеву локацію з подорожі.
   *
   * @param journeyId ID подорожі.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey removeDestinationLocationForJourney(Long journeyId, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);
    journey.setDestinationLocationId(null);
    journey.setDestinationLocation(null);
    return journeyRepository.save(journey);
  }

  /**
   * Повертає список подорожей, у яких користувач є учасником.
   *
   * @param currentUser Поточний автентифікований користувач.
   * @return Список об'єктів Journey.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Journey> getParticipatedJourneys(User currentUser) {
    return journeyRepository.findByParticipantId(currentUser.getId());
  }

  /**
   * Додає учасника до подорожі.
   *
   * @param journeyId ID подорожі.
   * @param participantIdentifier Ім'я користувача або електронна пошта учасника.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож або учасник не знайдено.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey addParticipantToJourney(
      Long journeyId, String participantIdentifier, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);

    User participant =
        userRepository
            .findByUsernameOrEmail(participantIdentifier) // Виправлено: findByUsernameOrEmail
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Учасника з ідентифікатором '" + participantIdentifier + "' не знайдено."));

    journey.getParticipants().add(participant);
    return journeyRepository.save(journey);
  }

  /**
   * Видаляє учасника з подорожі.
   *
   * @param journeyId ID подорожі.
   * @param participantIdentifier Ім'я користувача або електронна пошта учасника.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож або учасник не знайдено.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey removeParticipantFromJourney(
      Long journeyId, String participantIdentifier, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(journeyId, currentUser);

    Optional<User> participantOpt =
        userRepository.findByUsernameOrEmail(
            participantIdentifier); // Виправлено: findByUsernameOrEmail
    if (participantOpt.isPresent()) {
      journey.getParticipants().removeIf(p -> p.getId().equals(participantOpt.get().getId()));
      return journeyRepository.save(journey);
    } else {
      throw new IllegalArgumentException(
          "Учасника з ідентифікатором '" + participantIdentifier + "' не знайдено.");
    }
  }

  /**
   * Повертає учасників певної подорожі.
   *
   * @param journeyId ID подорожі.
   * @return Набір об'єктів User.
   */
  @Override
  @Transactional(readOnly = true)
  public Set<User> getParticipantsForJourney(Long journeyId) {
    return journeyRepository.findParticipantsByJourneyId(journeyId);
  }

  /**
   * Здійснює пошук подорожей за ключовим словом.
   *
   * @param keyword Ключове слово для пошуку.
   * @param currentUser Поточний автентифікований користувач.
   * @return Список об'єктів Journey.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Journey> searchJourneys(String keyword, User currentUser) {
    return journeyRepository.searchJourneys(keyword);
  }

  /**
   * Фільтрує подорожі за назвою тегу.
   *
   * @param tagName Назва тегу.
   * @param currentUser Поточний автентифікований користувач.
   * @return Список об'єктів Journey.
   */
  @Override
  @Transactional(readOnly = true)
  public List<Journey> filterJourneysByTag(String tagName, User currentUser) {
    Optional<Tag> tagOpt = tagRepository.findByName(tagName);
    if (tagOpt.isPresent()) {
      return journeyRepository.findByTagId(tagOpt.get().getId());
    } else {
      return List.of(); // Повертаємо порожній список, якщо тег не знайдено
    }
  }

  /**
   * Оновлює існуючу подорож.
   *
   * @param id ID подорожі.
   * @param updateDto Об'єкт JourneyUpdateDto з оновленими даними.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена або DTO недійсний.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public Journey updateJourney(Long id, JourneyUpdateDto updateDto, User currentUser) {
    Set<ConstraintViolation<JourneyUpdateDto>> violations = validator.validate(updateDto);
    if (!violations.isEmpty()) {
      throw new IllegalArgumentException(
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining("; ")));
    }

    Journey journey = getJourneyAndCheckOwnership(id, currentUser);

    Optional.ofNullable(updateDto.getName()).ifPresent(journey::setName);
    Optional.ofNullable(updateDto.getDescription()).ifPresent(journey::setDescription);
    Optional.ofNullable(updateDto.getStartDate()).ifPresent(journey::setStartDate);
    Optional.ofNullable(updateDto.getEndDate()).ifPresent(journey::setEndDate);

    // Оновлення початкової локації
    if (updateDto.getOriginLocationName() != null) {
      if (updateDto.getOriginLocationName().isEmpty()) {
        journey.setOriginLocationId(null);
        journey.setOriginLocation(null);
      } else {
        Location originLocation =
            locationRepository
                .findByName(updateDto.getOriginLocationName())
                .orElseGet(
                    () -> {
                      Location newLocation = new Location();
                      newLocation.setName(updateDto.getOriginLocationName());
                      // Виправлено: перевіряємо наявність getOriginLocationDescription()
                      newLocation.setDescription(
                          updateDto.getOriginLocationDescription() != null
                              ? updateDto.getOriginLocationDescription()
                              : null);
                      return locationRepository.save(newLocation);
                    });
        journey.setOriginLocationId(originLocation.getId());
        journey.setOriginLocation(originLocation);
      }
    }

    // Оновлення кінцевої локації
    if (updateDto.getDestinationLocationName() != null) {
      if (updateDto.getDestinationLocationName().isEmpty()) {
        journey.setDestinationLocationId(null);
        journey.setDestinationLocation(null);
      } else {
        Location destinationLocation =
            locationRepository
                .findByName(updateDto.getDestinationLocationName())
                .orElseGet(
                    () -> {
                      Location newLocation = new Location();
                      newLocation.setName(updateDto.getDestinationLocationName());
                      // Виправлено: перевіряємо наявність getDestinationLocationDescription()
                      newLocation.setDescription(
                          updateDto.getDestinationLocationDescription() != null
                              ? updateDto.getDestinationLocationDescription()
                              : null);
                      return locationRepository.save(newLocation);
                    });
        journey.setDestinationLocationId(destinationLocation.getId());
        journey.setDestinationLocation(destinationLocation);
      }
    }

    // Оновлення тегів (використовуємо getTagNames() замість getTags())
    if (updateDto.getTagNames() != null) {
      Set<Tag> updatedTags =
          updateDto.getTagNames().stream() // Виправлено: getTagNames()
              .map(
                  tagName ->
                      tagRepository
                          .findByName(tagName)
                          .orElseGet(
                              () -> {
                                Tag newTag = new Tag();
                                newTag.setName(tagName);
                                return tagRepository.save(newTag);
                              }))
              .collect(Collectors.toSet());
      journey.setTags(updatedTags);
    }

    journey.setUpdatedAt(LocalDateTime.now());
    return journeyRepository.save(journey);
  }

  /**
   * Видаляє подорож за її ID.
   *
   * @param id ID подорожі, яку потрібно видалити.
   * @param currentUser Поточний автентифікований користувач.
   * @return Оновлений об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  @Override
  @Transactional
  public void deleteJourney(Long id, User currentUser) {
    Journey journey = getJourneyAndCheckOwnership(id, currentUser);
    journeyRepository.deleteById(id);
  }

  /**
   * Допоміжний метод для отримання подорожі та перевірки прав власності.
   *
   * @param journeyId ID подорожі.
   * @param currentUser Поточний автентифікований користувач.
   * @return Об'єкт Journey.
   * @throws IllegalArgumentException якщо подорож не знайдена.
   * @throws SecurityException якщо користувач не є власником подорожі.
   */
  private Journey getJourneyAndCheckOwnership(Long journeyId, User currentUser) {
    return journeyRepository
        .findById(journeyId)
        .filter(j -> j.getUserId().equals(currentUser.getId()))
        .orElseThrow(
            () ->
                new SecurityException(
                    "Ви не маєте дозволу на виконання цієї дії з подорожжю ID " + journeyId));
  }
}
