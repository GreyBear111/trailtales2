package com.trailtales.service;

import com.trailtales.dto.EventCreationDto;
import com.trailtales.entity.Event;
import com.trailtales.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Сервіс для управління подіями, пов'язаними з подорожами. Дозволяє створювати, отримувати,
 * оновлювати та видаляти події.
 */
public interface EventService {

  /**
   * Створює нову подію на основі наданих даних.
   *
   * @param createDto об'єкт {@link EventCreationDto}, що містить дані для створення події.
   * @param currentUser поточний автентифікований користувач, який створює подію.
   * @return створений об'єкт {@link Event}.
   * @throws IllegalArgumentException якщо дані в DTO недійсні або пов'язана подорож не знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу додавати подію до вказаної
   *     подорожі.
   */
  Event createEvent(EventCreationDto createDto, User currentUser);

  /**
   * Знаходить подію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор події.
   * @return {@link Optional} з подією, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Event> getEventById(Long id);

  /**
   * Повертає список усіх існуючих подій.
   *
   * @return список {@link Event}.
   */
  List<Event> getAllEvents();

  /**
   * Повертає список усіх подій, пов'язаних з певною подорожжю.
   *
   * @param journeyId ID подорожі.
   * @return список {@link Event}, пов'язаних із вказаною подорожжю.
   */
  List<Event> getEventsByJourneyId(Long journeyId);

  /**
   * Оновлює існуючу подію. Об'єкт {@code event} повинен містити ID події, яку потрібно оновити, а
   * також поля, які підлягають оновленню.
   *
   * @param eventUpdates об'єкт {@link Event} з оновленими даними та ID існуючої події.
   * @param currentUser поточний автентифікований користувач, який виконує оновлення.
   * @return оновлений об'єкт {@link Event}.
   * @throws IllegalArgumentException якщо подія з вказаним ID не знайдена, або якщо надані дані для
   *     оновлення некоректні.
   * @throws SecurityException якщо поточний користувач не має дозволу оновлювати цю подію.
   */
  Event updateEvent(Event eventUpdates, User currentUser);

  /**
   * Видаляє подію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор події для видалення.
   * @param currentUser поточний автентифікований користувач, який виконує видалення.
   * @throws IllegalArgumentException якщо подія з вказаним ID не знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу видаляти цю подію.
   */
  void deleteEvent(Long id, User currentUser);

  /**
   * Знаходить події за ID подорожі, наданим у вигляді рядка. Цей метод може бути корисним для
   * обробки вхідних даних з консолі або інших джерел, де ID може бути представлений як рядок.
   *
   * @param journeyIdString ID подорожі у вигляді рядка.
   * @return список {@link Event}, пов'язаних з вказаною подорожжю.
   * @throws IllegalArgumentException якщо рядок не є дійсним числовим ID.
   */
  List<Event> findByJourneyId(String journeyIdString);
}
