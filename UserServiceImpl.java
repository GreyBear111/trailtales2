package com.trailtales.service.impl;

import com.trailtales.dto.UserLoginDto;
import com.trailtales.dto.UserRegistrationDto;
import com.trailtales.entity.Role;
import com.trailtales.entity.RoleName;
import com.trailtales.entity.User;
import com.trailtales.exception.AuthenticationException;
import com.trailtales.exception.UserAlreadyExistsException;
import com.trailtales.repository.RoleRepository;
import com.trailtales.repository.UserRepository;
import com.trailtales.service.UserService;
import com.trailtales.util.PasswordHasher;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordHasher passwordHasher;
  private final Validator validator;

  @Autowired
  public UserServiceImpl(
          UserRepository userRepository,
          RoleRepository roleRepository,
          PasswordHasher passwordHasher,
          Validator validator) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordHasher = passwordHasher;
    this.validator = validator;
  }

  @Override
  @Transactional
  public User registerUser(UserRegistrationDto registrationDto) {
    Set<ConstraintViolation<UserRegistrationDto>> violations = validator.validate(registrationDto);
    if (!violations.isEmpty()) {
      String errorMessages =
              violations.stream()
                      .map(ConstraintViolation::getMessage)
                      .collect(Collectors.joining("; "));
      throw new IllegalArgumentException(errorMessages);
    }

    if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
      throw new UserAlreadyExistsException(
              "Користувач з іменем '" + registrationDto.getUsername() + "' вже існує.");
    }
    if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
      throw new UserAlreadyExistsException(
              "Користувач з email '" + registrationDto.getEmail() + "' вже існує.");
    }

    String hashedPassword = passwordHasher.hashPassword(registrationDto.getPassword());

    User newUser = new User();
    newUser.setUsername(registrationDto.getUsername());
    newUser.setEmail(registrationDto.getEmail());
    newUser.setPasswordHash(hashedPassword);
    newUser.setCreatedAt(LocalDateTime.now());
    newUser.setUpdatedAt(LocalDateTime.now());

    Role userRole =
            roleRepository
                    .findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("Роль ROLE_USER не знайдена в базі даних."));
    Set<Role> roles = new HashSet<>();
    roles.add(userRole);
    newUser.setRoles(roles);

    return userRepository.save(newUser);
  }

  @Override
  @Transactional(readOnly = true)
  public User loginUser(UserLoginDto loginDto) {
    Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(loginDto);
    if (!violations.isEmpty()) {
      String errorMessages =
              violations.stream()
                      .map(ConstraintViolation::getMessage)
                      .collect(Collectors.joining("; "));
      throw new IllegalArgumentException(errorMessages);
    }

    String identifier = loginDto.getIdentifier();
    String password = loginDto.getPassword();

    if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
      throw new AuthenticationException("Ідентифікатор та пароль не можуть бути порожніми.");
    }

    User user = userRepository.findByUsernameOrEmail(identifier.trim())
            .orElseThrow(() -> new AuthenticationException("Невірний ідентифікатор або пароль."));

    if (!passwordHasher.checkPassword(password, user.getPasswordHash())) {
      throw new AuthenticationException("Невірний ідентифікатор або пароль.");
    }
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
  }
}