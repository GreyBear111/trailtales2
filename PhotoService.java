package com.trailtales.service;

import com.trailtales.dto.PhotoUploadDto;
import com.trailtales.entity.Photo;
import com.trailtales.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Сервіс для управління фотографіями, пов'язаними з подорожами. Надає функціонал для завантаження,
 * отримання, оновлення та видалення фотографій.
 */
public interface PhotoService {

  /**
   * Завантажує нову фотографію та пов'язує її з подорожжю. Передбачає збереження файлу фотографії у
   * визначене сховище.
   *
   * @param uploadDto об'єкт {@link PhotoUploadDto}, що містить дані для завантаження фото,
   *     включаючи ID подорожі та шлях до файлу.
   * @param currentUser поточний автентифікований користувач, який завантажує фото.
   * @return створений об'єкт {@link Photo} з інформацією про збережену фотографію.
   * @throws IllegalArgumentException якщо дані в DTO недійсні, подорож не знайдена, або виникла
   *     помилка під час обробки файлу.
   * @throws SecurityException якщо поточний користувач не має дозволу завантажувати фото до
   *     вказаної подорожі.
   * @throws RuntimeException якщо виникла помилка під час копіювання файлу.
   */
  Photo uploadPhoto(PhotoUploadDto uploadDto, User currentUser);

  /**
   * Знаходить фотографію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор фотографії.
   * @return {@link Optional} з фотографією, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<Photo> getPhotoById(Long id);

  /**
   * Повертає список усіх фотографій, пов'язаних з певною подорожжю.
   *
   * @param journeyId ID подорожі.
   * @return список {@link Photo}, пов'язаних із вказаною подорожжю.
   */
  List<Photo> getPhotosByJourneyId(Long journeyId);

  /**
   * Оновлює опис існуючої фотографії.
   *
   * @param photoId ID фотографії, опис якої потрібно оновити.
   * @param newDescription новий опис для фотографії.
   * @param currentUser поточний автентифікований користувач, який виконує оновлення.
   * @return оновлений об'єкт {@link Photo}.
   * @throws IllegalArgumentException (або специфічний `PhotoNotFoundException`) якщо фотографія з
   *     вказаним ID не знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу оновлювати цю фотографію.
   */
  Photo updatePhotoDescription(Long photoId, String newDescription, User currentUser);

  /**
   * Видаляє фотографію за її унікальним ідентифікатором. Передбачає також видалення файлу
   * фотографії зі сховища.
   *
   * @param id унікальний ідентифікатор фотографії для видалення.
   * @param currentUser поточний автентифікований користувач, який виконує видалення.
   * @throws IllegalArgumentException (або специфічний `PhotoNotFoundException`) якщо фотографія з
   *     вказаним ID не знайдена.
   * @throws SecurityException якщо поточний користувач не має дозволу видаляти цю фотографію.
   */
  void deletePhoto(Long id, User currentUser);
}
