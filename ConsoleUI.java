package com.trailtales.ui;

import com.trailtales.config.AppConfig;
import com.trailtales.dto.*;
import com.trailtales.entity.*;
import com.trailtales.exception.AuthenticationException;
import com.trailtales.exception.UserAlreadyExistsException;
import com.trailtales.service.*;
import com.trailtales.util.DatabaseInitializer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.File; // Для перевірки файлу для фото
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ConsoleUI {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static UserService userService;
    private static JourneyService journeyService;
    private static TagService tagService;
    private static LocationService locationService;
    private static EventService eventService;
    private static PhotoService photoService;
    private static DatabaseInitializer databaseInitializer;
    private static Validator validator; // Для ручної валідації DTO
    private static com.trailtales.repository.LocationRepository
            locationRepository; // ДОДАНО: Статичне поле для LocationRepository

    private static User currentUser = null;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        userService = context.getBean(UserService.class);
        journeyService = context.getBean(JourneyService.class);
        tagService = context.getBean(TagService.class);
        locationService = context.getBean(LocationService.class);
        eventService = context.getBean(EventService.class);
        photoService = context.getBean(PhotoService.class);
        databaseInitializer = context.getBean(DatabaseInitializer.class);
        validator = context.getBean(Validator.class);
        locationRepository =
                context.getBean(
                        com.trailtales.repository.LocationRepository.class); // ДОДАНО: Ініціалізація

        // Ініціалізуємо базу даних
        databaseInitializer.initialize(); // Тепер метод public

        mainMenu();
    }

    private static void mainMenu() {
        while (true) {
            System.out.println("\n--- Головне меню ---");
            if (currentUser == null) {
                System.out.println("1. Реєстрація");
                System.out.println("2. Вхід");
            } else {
                System.out.println(
                        "Ви увійшли як: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
                System.out.println("3. Меню подорожей");
                System.out.println("4. Меню тегів");
                System.out.println("5. Меню локацій");
                System.out.println("6. Меню подій");
                System.out.println("7. Меню фотографій");
                System.out.println("8. Вийти");
            }
            System.out.println("0. Вихід з програми");
            System.out.print("Виберіть опцію: ");

            int choice = getIntInput();

            if (currentUser == null) {
                switch (choice) {
                    case 1:
                        registerUser();
                        break;
                    case 2:
                        loginUser();
                        break;
                    case 0:
                        System.out.println("Вихід з програми.");
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            } else {
                switch (choice) {
                    case 3:
                        journeyMenu();
                        break;
                    case 4:
                        tagMenu();
                        break;
                    case 5:
                        locationMenu();
                        break;
                    case 6:
                        eventMenu();
                        break;
                    case 7:
                        photoMenu();
                        break;
                    case 8:
                        currentUser = null;
                        System.out.println("Ви успішно вийшли.");
                        break;
                    case 0:
                        System.out.println("Вихід з програми.");
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            }
        }
    }

    private static void registerUser() {
        System.out.println("\n--- Реєстрація нового користувача ---");
        System.out.print("Введіть ім'я користувача: ");
        String username = scanner.nextLine();
        System.out.print("Введіть Email: ");
        String email = scanner.nextLine();
        System.out.print("Введіть пароль: ");
        String password = scanner.nextLine();

        UserRegistrationDto registrationDto = new UserRegistrationDto(username, email, password);
        Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(registrationDto);
        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return; // Або просто завершити метод, щоб користувач спробував знову з меню
        }

        try {
            User registeredUser = userService.registerUser(registrationDto);
            System.out.println(
                    "Користувач '" + registeredUser.getUsername() + "' успішно зареєстрований!");
        } catch (UserAlreadyExistsException e) { // Специфічний виняток
            System.out.println("Помилка реєстрації: " + e.getMessage());
        } catch (IllegalArgumentException e) { // Для помилок валідації з сервісу або інших аргументів
            System.out.println("Помилка вхідних даних: " + e.getMessage());
        } catch (Exception e) { // Будь-які інші непередбачені помилки
            System.out.println("Сталася непередбачена помилка під час реєстрації: " + e.getMessage());
        }
    }

    private static void loginUser() {
        System.out.println("\n--- Вхід ---");
        System.out.print("Введіть ім'я користувача або Email: ");
        String identifier = scanner.nextLine();
        System.out.print("Введіть пароль: ");
        String password = scanner.nextLine();

        UserLoginDto loginDto = new UserLoginDto(identifier, password);
        // Валідація DTO на рівні UI
        Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(loginDto);
        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            // mainMenu();
            return;
        }

        try {
            currentUser = userService.loginUser(loginDto);
            System.out.println("Ласкаво просимо, " + currentUser.getUsername() + "!");
            // showMainMenu(); // Цей метод, ймовірно, не існує, перехід до головного циклу mainMenu() відбудеться автоматично
        } catch (AuthenticationException e) { // Обробка специфічного винятку
            System.out.println("Помилка входу: " + e.getMessage());
        } catch (IllegalArgumentException e) { // Для помилок валідації з сервісу
            System.out.println("Помилка вхідних даних: " + e.getMessage());
        } catch (Exception e) { // Будь-які інші непередбачені помилки
            System.out.println("Сталася непередбачена помилка під час входу: " + e.getMessage());
            // e.printStackTrace();
        }
    }

    private static void journeyMenu() {
        if (currentUser == null) {
            System.out.println("Будь ласка, увійдіть, щоб отримати доступ до меню подорожей.");
            return;
        }

        while (true) {
            System.out.println("\n--- Меню подорожей ---");
            System.out.println("1. Створити подорож");
            System.out.println("2. Переглянути мої подорожі");
            System.out.println("3. Переглянути подорожі, в яких я є учасником");
            System.out.println("4. Переглянути всі подорожі");
            System.out.println("5. Знайти подорож за ID");
            System.out.println("6. Оновити подорож");
            System.out.println("7. Видалити подорож");
            System.out.println("8. Додати тег до подорожі");
            System.out.println("9. Видалити тег з подорожі");
            System.out.println("10. Додати учасника до подорожі");
            System.out.println("11. Видалити учасника з подорожі");
            System.out.println("12. Пошук подорожей за ключовим словом");
            System.out.println("13. Фільтрувати подорожі за тегом");
            System.out.println("0. Назад до головного меню");
            System.out.print("Виберіть опцію: ");

            int choice = getIntInput();

            try {
                switch (choice) {
                    case 1:
                        createJourney();
                        break;
                    case 2:
                        listMyJourneys();
                        break;
                    case 3:
                        listParticipatedJourneys();
                        break;
                    case 4:
                        listAllJourneys();
                        break;
                    case 5:
                        findJourneyById();
                        break;
                    case 6:
                        updateJourney();
                        break;
                    case 7:
                        deleteJourney();
                        break;
                    case 8:
                        addTagToJourney();
                        break;
                    case 9:
                        removeTagFromJourney();
                        break;
                    case 10:
                        addParticipantToJourney();
                        break;
                    case 11:
                        removeParticipantFromJourney();
                        break;
                    case 12:
                        searchJourneys();
                        break;
                    case 13:
                        filterJourneysByTag();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
        }
    }

    private static void createJourney() {
        System.out.println("\n--- Створення нової подорожі ---");
        System.out.print("Назва подорожі: ");
        String name = scanner.nextLine();
        System.out.print("Опис подорожі (необов'язково): ");
        String description = scanner.nextLine();
      System.out.print("Дата початку (дд.мм.рррр, необов'язково): ");
      LocalDate startDate = parseDate(scanner.nextLine());
      System.out.print("Дата закінчення (дд.мм.рррр, необов'язково): ");
      LocalDate endDate = parseDate(scanner.nextLine());
        System.out.print("Теги (через кому, наприклад: гори, море, похід, необов'язково): ");
        String tagsInput = scanner.nextLine();
        Set<String> tagNames =
                tagsInput.isEmpty()
                        ? Set.of()
                        : Arrays.stream(tagsInput.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toSet());

        System.out.print("Назва початкової локації (необов'язково): ");
        String originLocationName = scanner.nextLine();
        System.out.print("Опис початкової локації (необов'язково): ");
        String originLocationDescription = scanner.nextLine(); // ДОДАНО

        System.out.print("Назва кінцевої локації (необов'язково): ");
        String destinationLocationName = scanner.nextLine();
        System.out.print("Опис кінцевої локації (необов'язково): ");
        String destinationLocationDescription = scanner.nextLine(); // ДОДАНО

        // Виправлено: використовуємо новий конструктор з Set<String> та описами локацій
        JourneyCreationDto createDto =
                new JourneyCreationDto(
                        name,
                        description,
                        startDate,
                        endDate,
                        tagNames, // Передаємо Set<String>
                        originLocationName,
                        originLocationDescription, // Передаємо опис
                        destinationLocationName,
                        destinationLocationDescription // Передаємо опис
                );

        Set<ConstraintViolation<JourneyCreationDto>> violations = validator.validate(createDto);
        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return;
        }

        try {
            Journey newJourney = journeyService.createJourney(createDto, currentUser);
            System.out.println(
                    "Подорож '" + newJourney.getName() + "' успішно створена з ID: " + newJourney.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка створення подорожі: " + e.getMessage());
        }
    }

    private static void listMyJourneys() {
        System.out.println("\n--- Мої подорожі ---");
        List<Journey> journeys = journeyService.getJourneysByUserId(currentUser.getId());
        if (journeys.isEmpty()) {
            System.out.println("У вас ще немає створених подорожей.");
            return;
        }
        journeys.forEach(ConsoleUI::displayJourneyDetails);
    }

    private static void listParticipatedJourneys() {
        System.out.println("\n--- Подорожі, в яких я є учасником ---");
        List<Journey> journeys = journeyService.getParticipatedJourneys(currentUser);
        if (journeys.isEmpty()) {
            System.out.println("Ви ще не є учасником жодної подорожі.");
            return;
        }
        journeys.forEach(ConsoleUI::displayJourneyDetails);
    }

    private static void listAllJourneys() {
        System.out.println("\n--- Усі подорожі ---");
        List<Journey> journeys = journeyService.getAllJourneys();
        if (journeys.isEmpty()) {
            System.out.println("Подорожей не знайдено.");
            return;
        }
        journeys.forEach(ConsoleUI::displayJourneyDetails);
    }

    private static void findJourneyById() {
        System.out.print("Введіть ID подорожі: ");
        Long journeyId = getLongInput();
        Optional<Journey> journeyOpt = journeyService.getJourneyById(journeyId);
        if (journeyOpt.isPresent()) {
            displayJourneyDetails(journeyOpt.get());
        } else {
            System.out.println("Подорож з ID " + journeyId + " не знайдено.");
        }
    }

    private static void updateJourney() {
        System.out.print("Введіть ID подорожі для оновлення: ");
        Long journeyId = getLongInput();
        Optional<Journey> existingJourneyOpt = journeyService.getJourneyById(journeyId);

        if (existingJourneyOpt.isEmpty()) {
            System.out.println("Подорож з ID " + journeyId + " не знайдено.");
            return;
        }

        Journey existingJourney = existingJourneyOpt.get();
        System.out.println("\n--- Оновлення подорожі (залиште пустим, щоб не змінювати) ---");
        System.out.println("Поточна назва: " + existingJourney.getName());
        System.out.print("Нова назва подорожі: ");
        String name = scanner.nextLine();
        System.out.println(
                "Поточний опис: "
                        + (existingJourney.getDescription() != null ? existingJourney.getDescription() : ""));
        System.out.print("Новий опис подорожі: ");
        String description = scanner.nextLine();
        System.out.println(
                "Поточна дата початку: "
                        + (existingJourney.getStartDate() != null ? existingJourney.getStartDate() : ""));
        System.out.print("Нова дата початку (РРРР-ММ-ДД): ");
        LocalDate startDate = parseDate(scanner.nextLine());
        System.out.println(
                "Поточна дата закінчення: "
                        + (existingJourney.getEndDate() != null ? existingJourney.getEndDate() : ""));
        System.out.print("Нова дата закінчення (РРРР-ММ-ДД): ");
        LocalDate endDate = parseDate(scanner.nextLine());

        System.out.println(
                "Поточні теги: "
                        + (existingJourney.getTags() != null
                        ? existingJourney.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.joining(", "))
                        : ""));
        System.out.print(
                "Нові теги (через кому, наприклад: гори, море, похід, або 'clear' для видалення всіх): ");
        String tagsInput = scanner.nextLine();
        Set<String> tagNames = null;
        if (tagsInput.equalsIgnoreCase("clear")) {
            tagNames = Set.of(); // Очистити всі теги
        } else if (!tagsInput.isEmpty()) {
            tagNames =
                    Arrays.stream(tagsInput.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toSet());
        }

        System.out.println(
                "Поточна початкова локація: "
                        + (existingJourney.getOriginLocation() != null
                        ? existingJourney.getOriginLocation().getName()
                        : ""));
        System.out.print("Нова назва початкової локації (або 'clear' для видалення): ");
        String originLocationName = scanner.nextLine();
        System.out.print("Новий опис початкової локації (необов'язково): ");
        String originLocationDescription = scanner.nextLine(); // ДОДАНО

        System.out.println(
                "Поточна кінцева локація: "
                        + (existingJourney.getDestinationLocation() != null
                        ? existingJourney.getDestinationLocation().getName()
                        : ""));
        System.out.print("Нова назва кінцевої локації (або 'clear' для видалення): ");
        String destinationLocationName = scanner.nextLine();
        System.out.print("Новий опис кінцевої локації (необов'язково): ");
        String destinationLocationDescription = scanner.nextLine(); // ДОДАНО

        // Виправлено: використовуємо новий конструктор з Set<String> та описами локацій
        JourneyUpdateDto updateDto =
                new JourneyUpdateDto(
                        name.isEmpty() ? null : name,
                        description.isEmpty() ? null : description,
                        startDate,
                        endDate,
                        tagNames, // Передаємо Set<String>
                        originLocationName.equalsIgnoreCase("clear")
                                ? ""
                                : (originLocationName.isEmpty() ? null : originLocationName),
                        originLocationDescription.isEmpty()
                                ? null
                                : originLocationDescription, // Передаємо опис
                        destinationLocationName.equalsIgnoreCase("clear")
                                ? ""
                                : (destinationLocationName.isEmpty() ? null : destinationLocationName),
                        destinationLocationDescription.isEmpty()
                                ? null
                                : destinationLocationDescription // Передаємо опис
                );

        Set<ConstraintViolation<JourneyUpdateDto>> violations = validator.validate(updateDto);
        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return;
        }

        try {
            Journey updatedJourney = journeyService.updateJourney(journeyId, updateDto, currentUser);
            System.out.println("Подорож з ID " + updatedJourney.getId() + " успішно оновлено.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка оновлення подорожі: " + e.getMessage());
        }
    }

    private static void deleteJourney() {
        System.out.print("Введіть ID подорожі для видалення: ");
        Long journeyId = getLongInput();
        try {
            journeyService.deleteJourney(journeyId, currentUser);
            System.out.println("Подорож з ID " + journeyId + " успішно видалено.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка видалення подорожі: " + e.getMessage());
        }
    }

    private static void addTagToJourney() {
        System.out.print("Введіть ID подорожі: ");
        Long journeyId = getLongInput();
        System.out.print("Введіть назву тегу для додавання: ");
        String tagName = scanner.nextLine();
        try {
            Journey updatedJourney = journeyService.addTagToJourney(journeyId, tagName, currentUser);
            System.out.println(
                    "Тег '" + tagName + "' успішно додано до подорожі ID " + updatedJourney.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка додавання тегу: " + e.getMessage());
        }
    }

    private static void removeTagFromJourney() {
        System.out.print("Введіть ID подорожі: ");
        Long journeyId = getLongInput();
        System.out.print("Введіть назву тегу для видалення: ");
        String tagName = scanner.nextLine();
        try {
            Journey updatedJourney = journeyService.removeTagFromJourney(journeyId, tagName, currentUser);
            System.out.println(
                    "Тег '" + tagName + "' успішно видалено з подорожі ID " + updatedJourney.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка видалення тегу: " + e.getMessage());
        }
    }

    private static void addParticipantToJourney() {
        System.out.print("Введіть ID подорожі: ");
        Long journeyId = getLongInput();
        System.out.print("Введіть ім'я користувача або Email учасника для додавання: ");
        String participantIdentifier = scanner.nextLine();
        try {
            Journey updatedJourney =
                    journeyService.addParticipantToJourney(journeyId, participantIdentifier, currentUser);
            System.out.println(
                    "Учасника '"
                            + participantIdentifier
                            + "' успішно додано до подорожі ID "
                            + updatedJourney.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка додавання учасника: " + e.getMessage());
        }
    }

    private static void removeParticipantFromJourney() {
        System.out.print("Введіть ID подорожі: ");
        Long journeyId = getLongInput();
        System.out.print("Введіть ім'я користувача або Email учасника для видалення: ");
        String participantIdentifier = scanner.nextLine();
        try {
            Journey updatedJourney =
                    journeyService.removeParticipantFromJourney(
                            journeyId, participantIdentifier, currentUser);
            System.out.println(
                    "Учасника '"
                            + participantIdentifier
                            + "' успішно видалено з подорожі ID "
                            + updatedJourney.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка видалення учасника: " + e.getMessage());
        }
    }

    private static void searchJourneys() {
        System.out.print("Введіть ключове слово для пошуку подорожей: ");
        String keyword = scanner.nextLine();
        List<Journey> journeys = journeyService.searchJourneys(keyword, currentUser);
        if (journeys.isEmpty()) {
            System.out.println("Подорожей за ключовим словом '" + keyword + "' не знайдено.");
            return;
        }
        journeys.forEach(ConsoleUI::displayJourneyDetails);
    }

    private static void filterJourneysByTag() {
        System.out.print("Введіть назву тегу для фільтрації подорожей: ");
        String tagName = scanner.nextLine();
        List<Journey> journeys = journeyService.filterJourneysByTag(tagName, currentUser);
        if (journeys.isEmpty()) {
            System.out.println("Подорожей з тегом '" + tagName + "' не знайдено.");
            return;
        }
        journeys.forEach(ConsoleUI::displayJourneyDetails);
    }

    private static void tagMenu() {
        if (currentUser == null) {
            System.out.println("Будь ласка, увійдіть, щоб отримати доступ до меню тегів.");
            return;
        }

        while (true) {
            System.out.println("\n--- Меню тегів ---");
            System.out.println("1. Створити тег");
            System.out.println("2. Переглянути всі теги");
            System.out.println("3. Знайти тег за ID");
            System.out.println("4. Знайти тег за назвою");
            System.out.println("5. Оновити тег");
            System.out.println("6. Видалити тег");
            System.out.println("7. Переглянути теги подорожі");
            System.out.println("0. Назад до головного меню");
            System.out.print("Виберіть опцію: ");

            int choice = getIntInput();

            try {
                switch (choice) {
                    case 1:
                        createTag();
                        break;
                    case 2:
                        listAllTags();
                        break;
                    case 3:
                        findTagById();
                        break;
                    case 4:
                        findTagByName();
                        break;
                    case 5:
                        updateTag();
                        break;
                    case 6:
                        deleteTag();
                        break;
                    case 7:
                        listTagsForJourney();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
        }
    }

    private static void createTag() {
        System.out.println("\n--- Створення нового тегу ---");
        System.out.print("Назва тегу: ");
        String name = scanner.nextLine();

        TagCreationDto createDto = new TagCreationDto(name);
        Set<ConstraintViolation<TagCreationDto>> violations = validator.validate(createDto);

        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return;
        }

        try {
            Tag newTag = tagService.createTag(name);
            System.out.println(
                    "Тег '" + newTag.getName() + "' успішно створений з ID: " + newTag.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка створення тегу: " + e.getMessage());
        }
    }

    private static void listAllTags() {
        System.out.println("\n--- Усі теги ---");
        List<Tag> tags = tagService.getAllTags();
        if (tags.isEmpty()) {
            System.out.println("Тегів не знайдено.");
            return;
        }
        tags.forEach(tag -> System.out.println("ID: " + tag.getId() + ", Назва: " + tag.getName()));
    }

    private static void findTagById() {
        System.out.print("Введіть ID тегу: ");
        Long tagId = getLongInput();
        Optional<Tag> tagOpt = tagService.getTagById(tagId);
        if (tagOpt.isPresent()) {
            Tag tag = tagOpt.get();
            System.out.println(
                    "ID: "
                            + tag.getId()
                            + ", Назва: "
                            + tag.getName()
                            + ", Створено: "
                            + tag.getCreatedAt()
                            + ", Оновлено: "
                            + tag.getUpdatedAt());
        } else {
            System.out.println("Тег з ID " + tagId + " не знайдено.");
        }
    }

    private static void findTagByName() {
        System.out.print("Введіть назву тегу: ");
        String tagName = scanner.nextLine();
        Optional<Tag> tagOpt = tagService.getTagByName(tagName);
        if (tagOpt.isPresent()) {
            Tag tag = tagOpt.get();
            System.out.println(
                    "ID: "
                            + tag.getId()
                            + ", Назва: "
                            + tag.getName()
                            + ", Створено: "
                            + tag.getCreatedAt()
                            + ", Оновлено: "
                            + tag.getUpdatedAt());
        } else {
            System.out.println("Тег з назвою '" + tagName + "' не знайдено.");
        }
    }

    private static void updateTag() {
        System.out.print("Введіть ID тегу для оновлення: ");
        Long tagId = getLongInput();
        System.out.print("Введіть нову назву тегу: ");
        String newName = scanner.nextLine();
        try {
            Tag updatedTag = tagService.updateTag(tagId, newName);
            System.out.println(
                    "Тег з ID "
                            + updatedTag.getId()
                            + " успішно оновлено на '"
                            + updatedTag.getName()
                            + "'.");
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка оновлення тегу: " + e.getMessage());
        }
    }

    private static void deleteTag() {
        System.out.print("Введіть ID тегу для видалення: ");
        Long tagId = getLongInput();
        try {
            tagService.deleteTag(tagId);
            System.out.println("Тег з ID " + tagId + " успішно видалено.");
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка видалення тегу: " + e.getMessage());
        }
    }

    private static void listTagsForJourney() {
        System.out.print("Введіть ID подорожі, для якої потрібно переглянути теги: ");
        Long journeyId = getLongInput();
        try {
            Set<Tag> tags = tagService.getTagsByJourneyId(journeyId);
            if (tags.isEmpty()) {
                System.out.println("Для подорожі з ID " + journeyId + " тегів не знайдено.");
                return;
            }
            System.out.println("Теги для подорожі ID " + journeyId + ":");
            tags.forEach(tag -> System.out.println("- ID: " + tag.getId() + ", Назва: " + tag.getName()));
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }

    private static void locationMenu() {
        if (currentUser == null) {
            System.out.println("Будь ласка, увійдіть, щоб отримати доступ до меню локацій.");
            return;
        }

        while (true) {
            System.out.println("\n--- Меню локацій ---");
            System.out.println("1. Створити локацію");
            System.out.println("2. Переглянути всі локації");
            System.out.println("3. Знайти локацію за ID");
            System.out.println("4. Знайти локацію за назвою");
            System.out.println("5. Оновити локацію");
            System.out.println("6. Видалити локацію");
            System.out.println("0. Назад до головного меню");
            System.out.print("Виберіть опцію: ");

            int choice = getIntInput();

            try {
                switch (choice) {
                    case 1:
                        createLocation();
                        break;
                    case 2:
                        listAllLocations();
                        break;
                    case 3:
                        findLocationById();
                        break;
                    case 4:
                        findLocationByName();
                        break;
                    case 5:
                        updateLocation();
                        break;
                    case 6:
                        deleteLocation();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
        }
    }

    private static void createLocation() {
        System.out.println("\n--- Створення нової локації ---");
        System.out.print("Назва локації: ");
        String name = scanner.nextLine();
        System.out.print("Опис локації (необов'язково): ");
        String description = scanner.nextLine();

        LocationCreationDto createDto = new LocationCreationDto(name, description);
        Set<ConstraintViolation<LocationCreationDto>> violations = validator.validate(createDto);

        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return;
        }

        try {
            Location newLocation = locationService.createLocation(name, description);
            System.out.println(
                    "Локація '" + newLocation.getName() + "' успішно створена з ID: " + newLocation.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка створення локації: " + e.getMessage());
        }
    }

    private static void listAllLocations() {
        System.out.println("\n--- Усі локації ---");
        List<Location> locations = locationService.getAllLocations();
        if (locations.isEmpty()) {
            System.out.println("Локацій не знайдено.");
            return;
        }
        locations.forEach(
                location ->
                        System.out.println(
                                "ID: "
                                        + location.getId()
                                        + ", Назва: "
                                        + location.getName()
                                        + ", Опис: "
                                        + (location.getDescription() != null ? location.getDescription() : "")));
    }

    private static void findLocationById() {
        System.out.print("Введіть ID локації: ");
        Long locationId = getLongInput();
        Optional<Location> locationOpt = locationService.getLocationById(locationId);
        if (locationOpt.isPresent()) {
            Location location = locationOpt.get();
            System.out.println(
                    "ID: "
                            + location.getId()
                            + ", Назва: "
                            + location.getName()
                            + ", Опис: "
                            + (location.getDescription() != null ? location.getDescription() : "")
                            + ", Створено: "
                            + location.getCreatedAt()
                            + ", Оновлено: "
                            + location.getUpdatedAt());
        } else {
            System.out.println("Локація з ID " + locationId + " не знайдено.");
        }
    }

    private static void findLocationByName() {
        System.out.print("Введіть назву локації: ");
        String locationName = scanner.nextLine();
        Optional<Location> locationOpt = locationService.getLocationByName(locationName);
        if (locationOpt.isPresent()) {
            Location location = locationOpt.get();
            System.out.println(
                    "ID: "
                            + location.getId()
                            + ", Назва: "
                            + location.getName()
                            + ", Опис: "
                            + (location.getDescription() != null ? location.getDescription() : "")
                            + ", Створено: "
                            + location.getCreatedAt()
                            + ", Оновлено: "
                            + location.getUpdatedAt());
        } else {
            System.out.println("Локація з назвою '" + locationName + "' не знайдено.");
        }
    }

    private static void updateLocation() {
        System.out.print("Введіть ID локації для оновлення: ");
        Long locationId = getLongInput();
        System.out.print("Введіть нову назву локації: ");
        String newName = scanner.nextLine();
        System.out.print("Введіть новий опис локації (необов'язково): ");
        String newDescription = scanner.nextLine();
        try {
            Location updatedLocation =
                    locationService.updateLocation(locationId, newName, newDescription);
            System.out.println("Локація з ID " + updatedLocation.getId() + " успішно оновлено.");
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка оновлення локації: " + e.getMessage());
        }
    }

    private static void deleteLocation() {
        System.out.print("Введіть ID локації для видалення: ");
        Long locationId = getLongInput();
        try {
            locationService.deleteLocation(locationId);
            System.out.println("Локація з ID " + locationId + " успішно видалено.");
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка видалення локації: " + e.getMessage());
        }
    }

    private static void eventMenu() {
        if (currentUser == null) {
            System.out.println("Будь ласка, увійдіть, щоб отримати доступ до меню подій.");
            return;
        }

        while (true) {
            System.out.println("\n--- Меню подій ---");
            System.out.println("1. Створити подію");
            System.out.println("2. Переглянути всі події");
            System.out.println("3. Знайти подію за ID");
            System.out.println("4. Переглянути події подорожі");
            System.out.println("5. Оновити подію");
            System.out.println("6. Видалити подію");
            System.out.println("0. Назад до головного меню");
            System.out.print("Виберіть опцію: ");

            int choice = getIntInput();

            try {
                switch (choice) {
                    case 1:
                        createEvent();
                        break;
                    case 2:
                        listAllEvents();
                        break;
                    case 3:
                        findEventById();
                        break;
                    case 4:
                        listEventsByJourneyId();
                        break;
                    case 5:
                        updateEvent();
                        break;
                    case 6:
                        deleteEvent();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
        }
    }

    private static void createEvent() {
        System.out.println("\n--- Створення нової події ---");
        System.out.print("Назва події: ");
        String name = scanner.nextLine();
        System.out.print("Опис події (необов'язково): ");
        String description = scanner.nextLine();
      System.out.print("Дата події (дд.мм.рррр, необов'язково): ");
      LocalDate eventDate = parseDate(scanner.nextLine());
        System.out.print("Час події (ГГ:ХХ, необов'язково): ");
        LocalTime eventTime = parseTime(scanner.nextLine());
        System.out.print(
                "ID подорожі, до якої належить подія (необов'язково, залиште пустим, якщо немає): ");
        Long journeyId = getLongInputOrNull();
        System.out.print("Назва локації події (необов'язково): ");
        String locationName = scanner.nextLine();
        System.out.print("Опис локації події (необов'язково): ");
        String locationDescription = scanner.nextLine(); // ДОДАНО

        // Виправлено: використовуємо новий конструктор з locationDescription
        EventCreationDto createDto =
                new EventCreationDto(
                        name,
                        description,
                        eventDate,
                        eventTime,
                        journeyId,
                        locationName,
                        locationDescription // Передаємо опис локації
                );

        Set<ConstraintViolation<EventCreationDto>> violations = validator.validate(createDto);
        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return;
        }

        try {
            Event newEvent = eventService.createEvent(createDto, currentUser);
            System.out.println(
                    "Подія '" + newEvent.getName() + "' успішно створена з ID: " + newEvent.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка створення події: " + e.getMessage());
        }
    }

    private static void listAllEvents() {
        System.out.println("\n--- Усі події ---");
        List<Event> events = eventService.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("Подій не знайдено.");
            return;
        }
        events.forEach(ConsoleUI::displayEventDetails);
    }

    private static void findEventById() {
        System.out.print("Введіть ID події: ");
        Long eventId = getLongInput();
        Optional<Event> eventOpt = eventService.getEventById(eventId);
        if (eventOpt.isPresent()) {
            displayEventDetails(eventOpt.get());
        } else {
            System.out.println("Подія з ID " + eventId + " не знайдено.");
        }
    }

    private static void listEventsByJourneyId() {
        System.out.print("Введіть ID подорожі, для якої потрібно переглянути події: ");
        String journeyIdString = scanner.nextLine(); // Отримуємо як String
        try {
            List<Event> events = eventService.findByJourneyId(journeyIdString);
            if (events.isEmpty()) {
                System.out.println("Для подорожі з ID " + journeyIdString + " подій не знайдено.");
                return;
            }
            System.out.println("Події для подорожі ID " + journeyIdString + ":");
            events.forEach(ConsoleUI::displayEventDetails);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }

    private static void updateEvent() {
        System.out.print("Введіть ID події для оновлення: ");
        Long eventId = getLongInput();
        Optional<Event> existingEventOpt = eventService.getEventById(eventId);

        if (existingEventOpt.isEmpty()) {
            System.out.println("Подію з ID " + eventId + " не знайдено.");
            return;
        }

        Event existingEvent = existingEventOpt.get();
        System.out.println("\n--- Оновлення події (залиште пустим, щоб не змінювати) ---");
        System.out.println("Поточна назва: " + existingEvent.getName());
        System.out.print("Нова назва події: ");
        String name = scanner.nextLine();
        System.out.println(
                "Поточний опис: "
                        + (existingEvent.getDescription() != null ? existingEvent.getDescription() : ""));
        System.out.print("Новий опис події: ");
        String description = scanner.nextLine();
        System.out.println(
                "Поточна дата події: "
                        + (existingEvent.getEventDate() != null ? existingEvent.getEventDate() : ""));
        System.out.print("Нова дата події (РРРР-ММ-ДД): ");
        LocalDate eventDate = parseDate(scanner.nextLine());
        System.out.println(
                "Поточний час події: "
                        + (existingEvent.getEventTime() != null ? existingEvent.getEventTime() : ""));
        System.out.print("Новий час події (ГГ:ХХ): ");
        LocalTime eventTime = parseTime(scanner.nextLine());
        System.out.println(
                "Поточна ID подорожі: "
                        + (existingEvent.getJourneyId() != null ? existingEvent.getJourneyId() : ""));
        System.out.print("Нова ID подорожі (залиште пустим, якщо немає, або 0 для відв'язки): ");
        Long journeyId = getLongInputOrNull();
        System.out.println(
                "Поточна локація: "
                        + (existingEvent.getLocationId() != null
                        ? locationService
                        .getLocationById(existingEvent.getLocationId())
                        .map(Location::getName)
                        .orElse("Невідомо")
                        : ""));
        System.out.print("Нова назва локації (або 'clear' для відв'язки): ");
        String locationName = scanner.nextLine();
        System.out.print("Новий опис локації (необов'язково): ");
        String locationDescription = scanner.nextLine(); // ДОДАНО

        // Створення нового Event об'єкта для оновлення, використовуючи існуючий ID
        Event eventToUpdate = new Event();
        eventToUpdate.setId(eventId);
        eventToUpdate.setName(name.isEmpty() ? existingEvent.getName() : name);
        eventToUpdate.setDescription(
                description.isEmpty() ? existingEvent.getDescription() : description);
        eventToUpdate.setEventDate(eventDate != null ? eventDate : existingEvent.getEventDate());
        eventToUpdate.setEventTime(eventTime != null ? eventTime : existingEvent.getEventTime());

        // Обробка journeyId
        if (journeyId != null) {
            if (journeyId == 0L) { // Спеціальне значення для відв'язки
                eventToUpdate.setJourneyId(null);
            } else {
                eventToUpdate.setJourneyId(journeyId);
            }
        } else {
            eventToUpdate.setJourneyId(existingEvent.getJourneyId());
        }

        // Обробка locationName та locationDescription
        if (locationName.equalsIgnoreCase("clear")) {
            eventToUpdate.setLocationId(null);
        } else if (!locationName.isEmpty()) {
            Location newLocation =
                    locationService
                            .getLocationByName(locationName)
                            .orElseGet(
                                    () -> {
                                        Location loc = new Location();
                                        loc.setName(locationName);
                                        loc.setDescription(locationDescription.isEmpty() ? null : locationDescription);
                                        return locationRepository.save(
                                                loc); // ВИПРАВЛЕНО: використання locationRepository
                                    });
            eventToUpdate.setLocationId(newLocation.getId());
        } else {
            eventToUpdate.setLocationId(existingEvent.getLocationId());
        }

        try {
            Event updatedEvent = eventService.updateEvent(eventToUpdate, currentUser);
            System.out.println("Подія з ID " + updatedEvent.getId() + " успішно оновлено.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка оновлення події: " + e.getMessage());
        }
    }

    private static void deleteEvent() {
        System.out.print("Введіть ID події для видалення: ");
        Long eventId = getLongInput();
        try {
            eventService.deleteEvent(eventId, currentUser);
            System.out.println("Подія з ID " + eventId + " успішно видалено.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка видалення події: " + e.getMessage());
        }
    }

    private static void photoMenu() {
        if (currentUser == null) {
            System.out.println("Будь ласка, увійдіть, щоб отримати доступ до меню фотографій.");
            return;
        }

        while (true) {
            System.out.println("\n--- Меню фотографій ---");
            System.out.println("1. Завантажити фотографію");
            System.out.println("2. Переглянути фотографії подорожі");
            System.out.println("3. Знайти фотографію за ID");
            System.out.println("4. Оновити опис фотографії");
            System.out.println("5. Видалити фотографію");
            System.out.println("0. Назад до головного меню");
            System.out.print("Виберіть опцію: ");

            int choice = getIntInput();

            try {
                switch (choice) {
                    case 1:
                        uploadPhoto();
                        break;
                    case 2:
                        listPhotosByJourneyId();
                        break;
                    case 3:
                        findPhotoById();
                        break;
                    case 4:
                        updatePhotoDescription();
                        break;
                    case 5:
                        deletePhoto();
                        break;
                    case 0:
                        return;
                    default:
                        System.out.println("Невірний вибір. Спробуйте ще.");
                }
            } catch (IllegalArgumentException | SecurityException e) {
                System.out.println("Помилка: " + e.getMessage());
            }
        }
    }

    private static void uploadPhoto() {
        System.out.println("\n--- Завантаження фотографії ---");
        System.out.print("Введіть ID подорожі, до якої належить фотографія: ");
        Long journeyId = getLongInput();
        System.out.print("Введіть шлях до файлу фотографії (наприклад, C:/photos/my_pic.jpg): ");
        String filePath = scanner.nextLine();
        System.out.print("Введіть опис фотографії (необов'язково): ");
        String description = scanner.nextLine();

        // Перевірка існування файлу
        File photoFile = new File(filePath);
        if (!photoFile.exists() || photoFile.isDirectory()) {
            System.out.println("Помилка: Файл за вказаним шляхом не існує або є директорією.");
            return;
        }

        PhotoUploadDto uploadDto = new PhotoUploadDto(journeyId, filePath, description);
        Set<ConstraintViolation<PhotoUploadDto>> violations = validator.validate(uploadDto);

        if (!violations.isEmpty()) {
            System.out.println("Помилки валідації:");
            violations.forEach(v -> System.out.println("- " + v.getMessage()));
            return;
        }

        try {
            Photo newPhoto = photoService.uploadPhoto(uploadDto, currentUser);
            System.out.println("Фотографія успішно завантажена з ID: " + newPhoto.getId());
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка завантаження фотографії: " + e.getMessage());
        }
    }

    private static void listPhotosByJourneyId() {
        System.out.print("Введіть ID подорожі, для якої потрібно переглянути фотографії: ");
        Long journeyId = getLongInput();
        List<Photo> photos = photoService.getPhotosByJourneyId(journeyId);
        if (photos.isEmpty()) {
            System.out.println("Для подорожі з ID " + journeyId + " фотографій не знайдено.");
            return;
        }
        System.out.println("Фотографії для подорожі ID " + journeyId + ":");
        photos.forEach(ConsoleUI::displayPhotoDetails);
    }

    private static void findPhotoById() {
        System.out.print("Введіть ID фотографії: ");
        Long photoId = getLongInput();
        Optional<Photo> photoOpt = photoService.getPhotoById(photoId);
        if (photoOpt.isPresent()) {
            displayPhotoDetails(photoOpt.get());
        } else {
            System.out.println("Фотографія з ID " + photoId + " не знайдено.");
        }
    }

    private static void updatePhotoDescription() {
        System.out.print("Введіть ID фотографії для оновлення опису: ");
        Long photoId = getLongInput();
        System.out.print("Введіть новий опис фотографії (залиште пустим, щоб очистити): ");
        String newDescription = scanner.nextLine();
        try {
            Photo updatedPhoto =
                    photoService.updatePhotoDescription(
                            photoId, newDescription.isEmpty() ? null : newDescription, currentUser);
            System.out.println("Опис фотографії з ID " + updatedPhoto.getId() + " успішно оновлено.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка оновлення опису фотографії: " + e.getMessage());
        }
    }

    private static void deletePhoto() {
        System.out.print("Введіть ID фотографії для видалення: ");
        Long photoId = getLongInput();
        try {
            photoService.deletePhoto(photoId, currentUser);
            System.out.println("Фотографія з ID " + photoId + " успішно видалено.");
        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println("Помилка видалення фотографії: " + e.getMessage());
        }
    }

    private static void displayJourneyDetails(Journey journey) {
        System.out.println("\n--- Деталі подорожі (ID: " + journey.getId() + ") ---");
        System.out.println("Назва: " + journey.getName());
        System.out.println(
                "Опис: " + (journey.getDescription() != null ? journey.getDescription() : ""));
        System.out.println(
                "Власник: " + (journey.getUser() != null ? journey.getUser().getUsername() : "Невідомо"));
        System.out.println(
                "Дата початку: " + (journey.getStartDate() != null ? journey.getStartDate() : ""));
        System.out.println(
                "Дата закінчення: " + (journey.getEndDate() != null ? journey.getEndDate() : ""));
        System.out.println(
                "Початкова локація: "
                        + (journey.getOriginLocation() != null ? journey.getOriginLocation().getName() : ""));
        System.out.println(
                "Кінцева локація: "
                        + (journey.getDestinationLocation() != null
                        ? journey.getDestinationLocation().getName()
                        : ""));

        Set<Tag> tags = journey.getTags();
        if (tags != null && !tags.isEmpty()) {
            System.out.println(
                    "Теги: " + tags.stream().map(Tag::getName).collect(Collectors.joining(", ")));
        } else {
            System.out.println("Теги: Відсутні");
        }

        Set<User> participants = journey.getParticipants();
        if (participants != null && !participants.isEmpty()) {
            System.out.println(
                    "Учасники: "
                            + participants.stream().map(User::getUsername).collect(Collectors.joining(", ")));
        } else {
            System.out.println("Учасники: Відсутні");
        }

        List<Event> events = journey.getEvents();
        if (events != null && !events.isEmpty()) {
            System.out.println("Події:");
            events.forEach(
                    event ->
                            System.out.println(
                                    "  - ID: "
                                            + event.getId()
                                            + ", Назва: "
                                            + event.getName()
                                            + ", Дата: "
                                            + event.getEventDate()));
        } else {
            System.out.println("Події: Відсутні");
        }

        List<Photo> photos = journey.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            System.out.println("Фотографії:");
            photos.forEach(
                    photo ->
                            System.out.println(
                                    "  - ID: "
                                            + photo.getId()
                                            + ", Шлях: "
                                            + photo.getFilePath()
                                            + ", Опис: "
                                            + (photo.getDescription() != null ? photo.getDescription() : "")));
        } else {
            System.out.println("Фотографії: Відсутні");
        }

        System.out.println("Створено: " + journey.getCreatedAt());
        System.out.println("Оновлено: " + journey.getUpdatedAt());
    }

    private static void displayEventDetails(Event event) {
        System.out.println("\n--- Деталі події (ID: " + event.getId() + ") ---");
        System.out.println("Назва: " + event.getName());
        System.out.println("Опис: " + (event.getDescription() != null ? event.getDescription() : ""));
        System.out.println("Дата: " + (event.getEventDate() != null ? event.getEventDate() : ""));
        System.out.println("Час: " + (event.getEventTime() != null ? event.getEventTime() : ""));
        System.out.println(
                "ID подорожі: " + (event.getJourneyId() != null ? event.getJourneyId() : ""));
        System.out.println(
                "Локація: "
                        + (event.getLocationId() != null
                        ? locationService
                        .getLocationById(event.getLocationId())
                        .map(Location::getName)
                        .orElse("Невідомо")
                        : ""));
        System.out.println("Створено: " + event.getCreatedAt());
        System.out.println("Оновлено: " + event.getUpdatedAt());
    }

    private static void displayPhotoDetails(Photo photo) {
        System.out.println("\n--- Деталі фотографії (ID: " + photo.getId() + ") ---");
        System.out.println("ID подорожі: " + photo.getJourneyId());
        System.out.println("Шлях до файлу: " + photo.getFilePath());
        System.out.println("Опис: " + (photo.getDescription() != null ? photo.getDescription() : ""));
        System.out.println("Створено: " + photo.getCreatedAt());
        System.out.println("Оновлено: " + photo.getUpdatedAt());
    }

    private static int getIntInput() {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Некоректне введення. Будь ласка, введіть число: ");
                scanner.next(); // Consume the invalid input
            }
        }
    }

    private static Long getLongInput() {
        while (true) {
            try {
                long input = scanner.nextLong();
                scanner.nextLine(); // Consume newline
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Некоректне введення. Будь ласка, введіть число: ");
                scanner.next(); // Consume the invalid input
            }
        }
    }

    private static Long getLongInputOrNull() {
        while (true) {
            try {
                String input = scanner.nextLine();
                if (input.trim().isEmpty()) {
                    return null;
                }
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.print("Некоректне введення. Будь ласка, введіть число або залиште пустим: ");
            }
        }
    }

    private static Long getLongInputFromString(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return null; // Повертаємо null, якщо не вдалося перетворити
        }
    }

    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER); // Використовуємо новий форматер
        } catch (DateTimeParseException e) {
            // Підказку також оновлюємо
            System.out.println("Помилка: Некоректний формат дати. Будь ласка, використовуйте формат дд.мм.рррр (наприклад, 25.12.2024).");
            return null;
        }
    }

    private static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(timeString);
        } catch (DateTimeParseException e) {
            System.out.println(
                    "Помилка: Некоректний формат часу. Використовуйте ГГ:ХХ. " + e.getMessage());
            return null;
        }
    }
}
