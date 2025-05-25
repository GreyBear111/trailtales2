package com.trailtales.repository;

import com.trailtales.entity.Event;
import com.trailtales.entity.Journey;
import com.trailtales.entity.Photo;
import com.trailtales.entity.Tag;
import com.trailtales.entity.User;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JourneyRepository {

  private final JdbcTemplate jdbcTemplate;
  private final LocationRepository locationRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final EventRepository eventRepository;
  private final PhotoRepository photoRepository;

  private final RowMapper<Journey> journeyRowMapper;

  public JourneyRepository(
      JdbcTemplate jdbcTemplate,
      LocationRepository locationRepository,
      UserRepository userRepository,
      TagRepository tagRepository,
      EventRepository eventRepository,
      PhotoRepository photoRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.locationRepository = locationRepository;
    this.userRepository = userRepository;
    this.tagRepository = tagRepository;
    this.eventRepository = eventRepository;
    this.photoRepository = photoRepository;

    this.journeyRowMapper =
        (rs, rowNum) -> {
          Journey journey = new Journey();
          journey.setId(rs.getLong("id"));
          Long userId = rs.getLong("user_id");
          journey.setUserId(userId);

          userRepository.findById(userId).ifPresent(journey::setUser);

          journey.setName(rs.getString("name"));
          journey.setDescription(rs.getString("description"));
          if (rs.getDate("start_date") != null) {
            journey.setStartDate(rs.getDate("start_date").toLocalDate());
          }
          if (rs.getDate("end_date") != null) {
            journey.setEndDate(rs.getDate("end_date").toLocalDate());
          }
          journey.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
          journey.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

          Long originLocationId = rs.getObject("origin_location_id", Long.class);
          journey.setOriginLocationId(originLocationId);
          if (originLocationId != null) {
            locationRepository.findById(originLocationId).ifPresent(journey::setOriginLocation);
          }

          Long destinationLocationId = rs.getObject("destination_location_id", Long.class);
          journey.setDestinationLocationId(destinationLocationId);
          if (destinationLocationId != null) {
            locationRepository
                .findById(destinationLocationId)
                .ifPresent(journey::setDestinationLocation);
          }

          Set<Tag> tags = findTagsByJourneyId(journey.getId());
          journey.setTags(tags);

          List<Event> events = eventRepository.findByJourneyId(journey.getId());
          journey.setEvents(events);

          List<Photo> photos = photoRepository.findByJourneyId(journey.getId());
          journey.setPhotos(photos);

          return journey;
        };
  }

  public Optional<Journey> findById(Long id) {
    String sql = "SELECT j.* FROM journeys j WHERE j.id = ?";
    try {
      Journey journey = jdbcTemplate.queryForObject(sql, journeyRowMapper, id);
      if (journey != null) {
        Set<User> participants = findParticipantsByJourneyId(journey.getId());
        journey.setParticipants(participants);
      }
      return Optional.ofNullable(journey);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public List<Journey> findAll() {
    String sql = "SELECT j.* FROM journeys j";
    List<Journey> journeys = jdbcTemplate.query(sql, journeyRowMapper);
    journeys.forEach(
        journey -> {
          Set<User> participants = findParticipantsByJourneyId(journey.getId());
          journey.setParticipants(participants);
        });
    return journeys;
  }

  public List<Journey> findByUserId(Long userId) {
    String sql = "SELECT j.* FROM journeys j WHERE j.user_id = ?";
    List<Journey> journeys = jdbcTemplate.query(sql, journeyRowMapper, userId);
    journeys.forEach(
        journey -> {
          Set<User> participants = findParticipantsByJourneyId(journey.getId());
          journey.setParticipants(participants);
        });
    return journeys;
  }

  public List<Journey> findByParticipantId(Long userId) {
    String sql =
        "SELECT j.* FROM journeys j JOIN journey_participants jp ON j.id = jp.journey_id WHERE jp.user_id = ?";
    List<Journey> journeys = jdbcTemplate.query(sql, journeyRowMapper, userId);
    journeys.forEach(
        journey -> {
          Set<User> participants = findParticipantsByJourneyId(journey.getId());
          journey.setParticipants(participants);
        });
    return journeys;
  }

  @Transactional
  public Journey save(Journey journey) {
    LocalDateTime now = LocalDateTime.now();
    if (journey.getId() == null) {
      String sql =
          "INSERT INTO journeys (user_id, name, description, start_date, end_date, created_at, updated_at, origin_location_id, destination_location_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
      KeyHolder keyHolder = new GeneratedKeyHolder();
      journey.setCreatedAt(now);
      journey.setUpdatedAt(now);

      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, journey.getUserId());
            ps.setString(2, journey.getName());
            ps.setString(3, journey.getDescription());
            ps.setObject(
                4,
                journey.getStartDate() != null
                    ? java.sql.Date.valueOf(journey.getStartDate())
                    : null,
                java.sql.Types.DATE);
            ps.setObject(
                5,
                journey.getEndDate() != null ? java.sql.Date.valueOf(journey.getEndDate()) : null,
                java.sql.Types.DATE);
            ps.setTimestamp(6, Timestamp.valueOf(journey.getCreatedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(journey.getUpdatedAt()));
            ps.setObject(8, journey.getOriginLocationId(), java.sql.Types.BIGINT);
            ps.setObject(9, journey.getDestinationLocationId(), java.sql.Types.BIGINT);
            return ps;
          },
          keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        journey.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException(
            "Не вдалося отримати згенерований ID після вставки подорожі.");
      }
    } else {
      String sql =
          "UPDATE journeys SET user_id = ?, name = ?, description = ?, start_date = ?, end_date = ?, updated_at = ?, origin_location_id = ?, destination_location_id = ? WHERE id = ?";
      journey.setUpdatedAt(now);
      jdbcTemplate.update(
          sql,
          journey.getUserId(),
          journey.getName(),
          journey.getDescription(),
          journey.getStartDate() != null ? java.sql.Date.valueOf(journey.getStartDate()) : null,
          journey.getEndDate() != null ? java.sql.Date.valueOf(journey.getEndDate()) : null,
          Timestamp.valueOf(journey.getUpdatedAt()),
          journey.getOriginLocationId(),
          journey.getDestinationLocationId(),
          journey.getId());
    }
    updateJourneyTags(journey);
    updateJourneyParticipants(journey);
    return journey;
  }

  @Transactional
  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM journey_tags WHERE journey_id = ?", id);
    jdbcTemplate.update("DELETE FROM journey_participants WHERE journey_id = ?", id);
    jdbcTemplate.update("DELETE FROM events WHERE journey_id = ?", id);
    jdbcTemplate.update("DELETE FROM photos WHERE journey_id = ?", id);
    jdbcTemplate.update("DELETE FROM journeys WHERE id = ?", id);
  }

  private void updateJourneyTags(Journey journey) {
    jdbcTemplate.update("DELETE FROM journey_tags WHERE journey_id = ?", journey.getId());
    if (journey.getTags() != null && !journey.getTags().isEmpty()) {
      List<Object[]> batchArgs =
          journey.getTags().stream()
              .map(
                  tag -> {
                    Long tagId = tag.getId();
                    if (tagId == null && tag.getName() != null) {
                      Optional<Tag> persistedTag = tagRepository.findByName(tag.getName());
                      if (persistedTag.isPresent()) {
                        tagId = persistedTag.get().getId();
                      } else {
                        throw new IllegalStateException(
                            "Тег з назвою "
                                + tag.getName()
                                + " не знайдено для подорожі "
                                + journey.getName());
                      }
                    }
                    if (tagId == null) {
                      throw new IllegalStateException(
                          "ID тегу не може бути null для подорожі " + journey.getName());
                    }
                    return new Object[] {journey.getId(), tagId};
                  })
              .collect(Collectors.toList());
      jdbcTemplate.batchUpdate(
          "INSERT INTO journey_tags (journey_id, tag_id) VALUES (?, ?)", batchArgs);
    }
  }

  private void updateJourneyParticipants(Journey journey) {
    jdbcTemplate.update("DELETE FROM journey_participants WHERE journey_id = ?", journey.getId());
    if (journey.getParticipants() != null && !journey.getParticipants().isEmpty()) {
      List<Object[]> batchArgs =
          journey.getParticipants().stream()
              .map(
                  user -> {
                    Long userId = user.getId();
                    if (userId == null && user.getUsername() != null) {
                      Optional<User> persistedUser =
                          userRepository.findByUsername(user.getUsername());
                      if (persistedUser.isPresent()) {
                        userId = persistedUser.get().getId();
                      } else {
                        throw new IllegalStateException(
                            "Користувач з іменем " + user.getUsername() + " не знайдений.");
                      }
                    }
                    if (userId == null) {
                      throw new IllegalStateException("ID користувача не може бути null.");
                    }
                    return new Object[] {journey.getId(), userId};
                  })
              .collect(Collectors.toList());
      jdbcTemplate.batchUpdate(
          "INSERT INTO journey_participants (journey_id, user_id) VALUES (?, ?)", batchArgs);
    }
  }

  public Set<Tag> findTagsByJourneyId(Long journeyId) {
    String sql =
        "SELECT t.id, t.name, t.created_at, t.updated_at FROM tags t JOIN journey_tags jt ON t.id = jt.tag_id WHERE jt.journey_id = ?";
    return new HashSet<>(
        jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
              Tag tag = new Tag();
              tag.setId(rs.getLong("id"));
              tag.setName(rs.getString("name"));
              tag.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
              tag.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
              return tag;
            },
            journeyId));
  }

  public void addParticipant(Long journeyId, Long userId) {
    String sql = "INSERT INTO journey_participants (journey_id, user_id) VALUES (?, ?)";
    jdbcTemplate.update(sql, journeyId, userId);
  }

  public void removeParticipant(Long journeyId, Long userId) {
    String sql = "DELETE FROM journey_participants WHERE journey_id = ? AND user_id = ?";
    jdbcTemplate.update(sql, journeyId, userId);
  }

  public Set<User> findParticipantsByJourneyId(Long journeyId) {
    String sql =
        "SELECT u.id, u.username, u.email, u.password_hash, u.created_at, u.updated_at "
            + "FROM users u JOIN journey_participants jp ON u.id = jp.user_id "
            + "WHERE jp.journey_id = ?";
    RowMapper<User> participantRowMapper =
        (rs, rowNum) -> {
          User user = new User();
          user.setId(rs.getLong("id"));
          user.setUsername(rs.getString("username"));
          user.setEmail(rs.getString("email"));
          user.setPasswordHash(rs.getString("password_hash"));
          user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
          user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
          user.setRoles(new HashSet<>());
          return user;
        };
    return new HashSet<>(jdbcTemplate.query(sql, participantRowMapper, journeyId));
  }

  public List<Journey> searchJourneys(String keyword) {
    String sql =
        "SELECT DISTINCT j.* FROM journeys j "
            + "LEFT JOIN locations origin_loc ON j.origin_location_id = origin_loc.id "
            + "LEFT JOIN locations dest_loc ON j.destination_location_id = dest_loc.id "
            + "LEFT JOIN journey_tags jt ON j.id = jt.journey_id "
            + "LEFT JOIN tags t ON jt.tag_id = t.id "
            + "WHERE j.name ILIKE ? OR j.description ILIKE ? OR t.name ILIKE ? "
            + "OR origin_loc.name ILIKE ? OR dest_loc.name ILIKE ?";
    String searchKeyword = "%" + keyword + "%";
    List<Journey> journeys =
        jdbcTemplate.query(
            sql,
            journeyRowMapper,
            searchKeyword,
            searchKeyword,
            searchKeyword,
            searchKeyword,
            searchKeyword);
    journeys.forEach(
        journey -> {
          Set<User> participants = findParticipantsByJourneyId(journey.getId());
          journey.setParticipants(participants);
        });
    return journeys;
  }

  public List<Journey> findByTagId(Long tagId) {
    String sql =
        "SELECT j.* FROM journeys j JOIN journey_tags jt ON j.id = jt.journey_id WHERE jt.tag_id = ?";
    List<Journey> journeys = jdbcTemplate.query(sql, journeyRowMapper, tagId);
    journeys.forEach(
        journey -> {
          Set<User> participants = findParticipantsByJourneyId(journey.getId());
          journey.setParticipants(participants);
        });
    return journeys;
  }
}
