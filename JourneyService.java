package com.trailtales.service;

import com.trailtales.dto.JourneyCreationDto;
import com.trailtales.dto.JourneyUpdateDto;
import com.trailtales.entity.Journey;
import com.trailtales.entity.Tag;
import com.trailtales.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Сервіс для управління подорожами. Надає повний набір операцій для створення, пошуку, оновлення,
 * видалення подорожей, а також управління їх атрибутами, такими як теги, локації та учасники.
 */
public interface JourneyService {

  /**
   * Створює нову подорож на основі наданих даних.
   *
   * @param createDto об'єкт {@link JourneyCreationDto}, що містить дані для створення подорожі.
   * @param currentUser поточний автентифікований користувач, який створює подорож.
   * @return створений об'єкт {@link Journey}.
   * @throws IllegalArgumentException якщо дані в DTO недійсні.
   */
  Journey createJourney(JourneyCreationDto createDto, User currentUser);

  /**
   * Знаходить подорож за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор подорожі.
   * @return {@link Optional} з подорожжю, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Journey> getJourneyById(Long id);

  /**
   * Повертає список усіх існуючих подорожей.
   *
   * @return список {@link Journey}.
   */
  List<Journey> getAllJourneys();

  /**
   * Повертає список подорожей, створених певним користувачем.
   *
   * @param userId ID користувача, подорожі якого потрібно знайти.
   * @return список {@link Journey}, створених вказаним користувачем.
   */
  List<Journey> getJourneysByUserId(Long userId);

  /**
   * Додає тег до вказаної подорожі. Якщо тег з такою назвою не існує, він буде створений.
   *
   * @param journeyId ID подорожі, до якої додається тег.
   * @param tagName назва тегу.
   * @param currentUser поточний автентифікований користувач, який виконує дію.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу змінювати цю подорож.
   */
  Journey addTagToJourney(Long journeyId, String tagName, User currentUser);

  /**
   * Видаляє тег з вказаної подорожі.
   *
   * @param journeyId ID подорожі, з якої видаляється тег.
   * @param tagName назва тегу для видалення.
   * @param currentUser поточний автентифікований користувач, який виконує дію.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена, або якщо тег з такою назвою не знайдено у подорожі.
   * @throws SecurityException якщо поточний користувач не має дозволу змінювати цю подорож.
   */
  Journey removeTagFromJourney(Long journeyId, String tagName, User currentUser);

  /**
   * Повертає набір тегів, пов'язаних з певною подорожжю.
   *
   * @param journeyId ID подорожі.
   * @return набір {@link Tag} для вказаної подорожі.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   */
  Set<Tag> getTagsForJourney(Long journeyId);

  /**
   * Встановлює або оновлює початкову локацію для подорожі. Якщо локація з такою назвою не існує,
   * вона буде створена.
   *
   * @param journeyId ID подорожі.
   * @param locationName назва початкової локації.
   * @param locationDescription опис початкової локації (використовується при створенні нової
   *     локації).
   * @param currentUser поточний автентифікований користувач.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу змінювати цю подорож.
   */
  Journey setOriginLocationForJourney(
      Long journeyId, String locationName, String locationDescription, User currentUser);

  /**
   * Встановлює або оновлює кінцеву локацію для подорожі. Якщо локація з такою назвою не існує, вона
   * буде створена.
   *
   * @param journeyId ID подорожі.
   * @param locationName назва кінцевої локації.
   * @param locationDescription опис кінцевої локації (використовується при створенні нової
   *     локації).
   * @param currentUser поточний автентифікований користувач.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу змінювати цю подорож.
   */
  Journey setDestinationLocationForJourney(
      Long journeyId, String locationName, String locationDescription, User currentUser);

  /**
   * Видаляє початкову локацію з подорожі (встановлює її в null).
   *
   * @param journeyId ID подорожі.
   * @param currentUser поточний автентифікований користувач.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу змінювати цю подорож.
   */
  Journey removeOriginLocationForJourney(Long journeyId, User currentUser);

  /**
   * Видаляє кінцеву локацію з подорожі (встановлює її в null).
   *
   * @param journeyId ID подорожі.
   * @param currentUser поточний автентифікований користувач.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу змінювати цю подорож.
   */
  Journey removeDestinationLocationForJourney(Long journeyId, User currentUser);

  /**
   * Повертає список подорожей, у яких вказаний користувач є учасником.
   *
   * @param currentUser користувач, для якого шукаються подорожі.
   * @return список {@link Journey}.
   */
  List<Journey> getParticipatedJourneys(User currentUser);

  /**
   * Додає користувача як учасника до вказаної подорожі.
   *
   * @param journeyId ID подорожі.
   * @param participantIdentifier ідентифікатор (ім'я користувача або email) учасника, якого
   *     потрібно додати.
   * @param currentUser поточний автентифікований користувач, який виконує дію (зазвичай власник
   *     подорожі).
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException` /
   *     `UserNotFoundException`) якщо подорож або учасник не знайдено, або якщо власник намагається
   *     додати себе як учасника.
   * @throws SecurityException якщо поточний користувач не має дозволу додавати учасників до цієї
   *     подорожі.
   */
  Journey addParticipantToJourney(Long journeyId, String participantIdentifier, User currentUser);

  /**
   * Видаляє користувача зі списку учасників вказаної подорожі.
   *
   * @param journeyId ID подорожі.
   * @param participantIdentifier ідентифікатор (ім'я користувача або email) учасника, якого
   *     потрібно видалити.
   * @param currentUser поточний автентифікований користувач, який виконує дію (зазвичай власник
   *     подорожі).
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException` /
   *     `UserNotFoundException`) якщо подорож або учасник не знайдено, або якщо учасник не є
   *     учасником подорожі, або якщо намагаються видалити власника подорожі.
   * @throws SecurityException якщо поточний користувач не має дозволу видаляти учасників з цієї
   *     подорожі.
   */
  Journey removeParticipantFromJourney(
      Long journeyId, String participantIdentifier, User currentUser);

  /**
   * Повертає набір учасників для певної подорожі.
   *
   * @param journeyId ID подорожі.
   * @return набір {@link User}, які є учасниками подорожі.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   */
  Set<User> getParticipantsForJourney(Long journeyId);

  /**
   * Здійснює пошук подорожей за ключовим словом у назві, описі, тегах або локаціях.
   *
   * @param keyword ключове слово для пошуку.
   * @param currentUser поточний автентифікований користувач (може використовуватися для фільтрації
   *     результатів у майбутньому).
   * @return список {@link Journey}, що відповідають критеріям пошуку.
   */
  List<Journey> searchJourneys(String keyword, User currentUser);

  /**
   * Фільтрує подорожі за назвою тегу.
   *
   * @param tagName назва тегу для фільтрації.
   * @param currentUser поточний автентифікований користувач (може використовуватися для фільтрації
   *     результатів у майбутньому).
   * @return список {@link Journey}, що мають вказаний тег.
   * @throws IllegalArgumentException якщо назва тегу порожня.
   */
  List<Journey> filterJourneysByTag(String tagName, User currentUser);

  /**
   * Оновлює основну інформацію існуючої подорожі.
   *
   * @param id ID подорожі, яку потрібно оновити.
   * @param updateDto об'єкт {@link JourneyUpdateDto} з даними для оновлення.
   * @param currentUser поточний автентифікований користувач, який виконує оновлення.
   * @return оновлений об'єкт {@link Journey}.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена або дані в DTO недійсні.
   * @throws SecurityException якщо поточний користувач не має дозволу оновлювати цю подорож.
   */
  Journey updateJourney(Long id, JourneyUpdateDto updateDto, User currentUser);

  /**
   * Видаляє подорож за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор подорожі для видалення.
   * @param currentUser поточний автентифікований користувач, який виконує видалення.
   * @throws IllegalArgumentException (або специфічний `JourneyNotFoundException`) якщо подорож не
   *     знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу видаляти цю подорож.
   */
  void deleteJourney(Long id, User currentUser);
}
