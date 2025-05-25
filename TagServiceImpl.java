package com.trailtales.service.impl;

import com.trailtales.entity.Tag;
import com.trailtales.repository.TagRepository;
import com.trailtales.service.TagService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepository;

  @Autowired
  public TagServiceImpl(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @Override
  @Transactional
  public Tag createTag(String name) {
    Optional<Tag> existingTag = tagRepository.findByName(name);
    if (existingTag.isPresent()) {
      throw new IllegalArgumentException("Тег з назвою '" + name + "' вже існує.");
    }

    Tag tag = new Tag();
    tag.setName(name);
    tag.setCreatedAt(LocalDateTime.now());
    tag.setUpdatedAt(LocalDateTime.now());
    return tagRepository.save(tag);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Tag> getTagById(Long id) {
    return tagRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Tag> getTagByName(String name) {
    return tagRepository.findByName(name);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Tag> getAllTags() {
    return tagRepository.findAll();
  }

  @Override
  @Transactional
  public Tag updateTag(Long id, String newName) {
    Optional<Tag> tagOpt = tagRepository.findById(id);
    if (tagOpt.isEmpty()) {
      throw new IllegalArgumentException("Тег з ID " + id + " не знайдено.");
    }
    Tag tag = tagOpt.get();

    if (!tag.getName().equals(newName)) {
      Optional<Tag> existingTagWithNewName = tagRepository.findByName(newName);
      if (existingTagWithNewName.isPresent()) {
        throw new IllegalArgumentException("Тег з назвою '" + newName + "' вже існує.");
      }
    }

    tag.setName(newName);
    tag.setUpdatedAt(LocalDateTime.now());
    return tagRepository.save(tag);
  }

  @Override
  @Transactional
  public void deleteTag(Long id) {
    if (tagRepository.findById(id).isEmpty()) {
      throw new IllegalArgumentException("Тег з ID " + id + " не знайдено для видалення.");
    }
    tagRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Tag> getTagsByJourneyId(Long journeyId) {
    return tagRepository.findTagsByJourneyId(journeyId);
  }
}
