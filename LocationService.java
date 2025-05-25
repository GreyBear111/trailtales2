package com.trailtales.service;

import com.trailtales.entity.Location;
import java.util.List;
import java.util.Optional;

/**
 * Сервіс для управління географічними локаціями. Надає операції для створення, пошуку, оновлення та
 * видалення локацій.
 */
public interface LocationService {

  /**
   * Створює нову локацію.
   *
   * @param name назва нової локації. Не може бути порожньою.
   * @param description опис локації (може бути null або порожнім).
   * @return створений об'єкт {@link Location}.
   * @throws IllegalArgumentException якщо назва локації порожня або локація з такою назвою вже
   *     існує.
   */
  Location createLocation(String name, String description);

  /**
   * Знаходить локацію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор локації.
   * @return {@link Optional} з локацією, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Location> getLocationById(Long id);

  /**
   * Знаходить локацію за її назвою.
   *
   * @param name назва локації.
   * @return {@link Optional} з локацією, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Location> getLocationByName(String name);

  /**
   * Повертає список усіх існуючих локацій.
   *
   * @return список {@link Location}.
   */
  List<Location> getAllLocations();

  /**
   * Оновлює назву та/або опис існуючої локації.
   *
   * @param id ID локації, яку потрібно оновити.
   * @param newName нова назва для локації (якщо не null і не порожня).
   * @param newDescription новий опис для локації (може бути null для видалення опису).
   * @return оновлений об'єкт {@link Location}.
   * @throws IllegalArgumentException (або специфічний `LocationNotFoundException`) якщо локація з
   *     вказаним ID не знайдена, або якщо локація з новою назвою вже існує (і це не та сама
   *     локація).
   */
  Location updateLocation(Long id, String newName, String newDescription);

  /**
   * Видаляє локацію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор локації для видалення.
   * @throws IllegalArgumentException (або специфічний `LocationNotFoundException`) якщо локація з
   *     вказаним ID не знайдена.
   */
  void deleteLocation(Long id);
}
