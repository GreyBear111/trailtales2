package com.trailtales.service.impl;

import com.trailtales.entity.Location;
import com.trailtales.repository.LocationRepository;
import com.trailtales.service.LocationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;

  @Autowired
  public LocationServiceImpl(LocationRepository locationRepository) {
    this.locationRepository = locationRepository;
  }

  @Override
  @Transactional
  public Location createLocation(String name, String description) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Назва локації не може бути порожньою.");
    }
    if (locationRepository.findByName(name).isPresent()) {
      throw new IllegalArgumentException("Локація з назвою '" + name + "' вже існує.");
    }
    Location newLocation = new Location();
    newLocation.setName(name);
    newLocation.setDescription(description);
    newLocation.setCreatedAt(LocalDateTime.now());
    newLocation.setUpdatedAt(LocalDateTime.now());
    return locationRepository.save(newLocation);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Location> getLocationById(Long id) {
    return locationRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Location> getLocationByName(String name) {
    return locationRepository.findByName(name);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Location> getAllLocations() {
    return locationRepository.findAll();
  }

  @Override
  @Transactional
  public Location updateLocation(Long id, String newName, String newDescription) {
    Location locationToUpdate =
        locationRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalArgumentException("Локація з ID " + id + " не знайдена."));

    if (newName != null && !newName.trim().isEmpty()) {
      if (!locationToUpdate.getName().equalsIgnoreCase(newName)
          && locationRepository.findByName(newName).isPresent()) {
        throw new IllegalArgumentException("Локація з назвою '" + newName + "' вже існує.");
      }
      locationToUpdate.setName(newName);
    }

    if (newDescription != null) {
      locationToUpdate.setDescription(newDescription.trim().isEmpty() ? null : newDescription);
    }

    locationToUpdate.setUpdatedAt(LocalDateTime.now());
    return locationRepository.save(locationToUpdate);
  }

  @Override
  @Transactional
  public void deleteLocation(Long id) {
    if (locationRepository.findById(id).isEmpty()) {
      throw new IllegalArgumentException("Локація з ID " + id + " не знайдена для видалення.");
    }
    locationRepository.deleteById(id);
  }
}
