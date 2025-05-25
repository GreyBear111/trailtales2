package com.trailtales.service;

import com.trailtales.entity.Tag;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Сервіс для управління тегами. Визначає операції для створення, пошуку, оновлення та видалення
 * тегів.
 */
public interface TagService {

  /**
   * Створює новий тег.
   *
   * @param name назва нового тегу.
   * @return створений об'єкт {@link Tag}.
   * @throws IllegalArgumentException якщо тег з такою назвою вже існує.
   */
  Tag createTag(String name);

  /**
   * Знаходить тег за його унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор тегу.
   * @return {@link Optional} з тегом, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Tag> getTagById(Long id);

  /**
   * Знаходить тег за його назвою.
   *
   * @param name назва тегу.
   * @return {@link Optional} з тегом, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Tag> getTagByName(String name);

  /**
   * Повертає список усіх існуючих тегів.
   *
   * @return список {@link Tag}.
   */
  List<Tag> getAllTags();

  /**
   * Оновлює назву існуючого тегу.
   *
   * @param id ID тегу, який потрібно оновити.
   * @param newName нова назва для тегу.
   * @return оновлений об'єкт {@link Tag}.
   * @throws IllegalArgumentException якщо тег з вказаним ID не знайдено, або якщо тег з новою
   *     назвою вже існує.
   */
  Tag updateTag(Long id, String newName);

  /**
   * Видаляє тег за його унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор тегу для видалення.
   * @throws IllegalArgumentException якщо тег з вказаним ID не знайдено.
   */
  void deleteTag(Long id);

  /**
   * Повертає набір тегів, пов'язаних з певною подорожжю.
   *
   * @param journeyId ID подорожі.
   * @return набір {@link Tag}, пов'язаних із вказаною подорожжю.
   */
  Set<Tag> getTagsByJourneyId(Long journeyId);
}
