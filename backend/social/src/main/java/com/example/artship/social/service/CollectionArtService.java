package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CollectionArtDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Collection;
import com.example.artship.social.model.CollectionArt;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CollectionArtRepository;
import com.example.artship.social.repository.CollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionArtService {
    
    private static final Logger log = LoggerFactory.getLogger(CollectionArtService.class);
    
    private final CollectionArtRepository collectionArtRepository;
    private final CollectionRepository collectionRepository;
    private final ArtRepository artRepository;
    private final ArtService artService;
    
    public CollectionArtService(CollectionArtRepository collectionArtRepository,
                              CollectionRepository collectionRepository,
                              ArtRepository artRepository,
                              ArtService artService) {
        this.collectionArtRepository = collectionArtRepository;
        this.collectionRepository = collectionRepository;
        this.artRepository = artRepository;
        this.artService = artService;
    }
    
    // Добавление арта в коллекцию
    public CollectionArtDto addArtToCollection(Long collectionId, Long artId) {
        log.info("Adding art {} to collection {}", artId, collectionId);
        
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> {
                    log.error("Collection not found with id: {}", collectionId);
                    return new RuntimeException("Collection not found with id: " + collectionId);
                });
        
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> {
                    log.error("Art not found with id: {}", artId);
                    return new RuntimeException("Art not found with id: " + artId);
                });
        
        // Проверяем, что арт публичный или пользователь является автором
        if (!art.getIsPublic() && !art.getAuthor().getId().equals(collection.getUser().getId())) {
            String error = "Cannot add private art to collection. Art author: " + 
                          art.getAuthor().getId() + ", Collection owner: " + collection.getUser().getId();
            log.error(error);
            throw new RuntimeException(error);
        }
        
        if (collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId)) {
            String error = "Art " + artId + " already exists in collection " + collectionId;
            log.error(error);
            throw new RuntimeException(error);
        }
        
        CollectionArt collectionArt = new CollectionArt(collection, art);
        CollectionArt savedCollectionArt = collectionArtRepository.save(collectionArt);
        
        log.info("Successfully added art {} to collection {}", artId, collectionId);
        return new CollectionArtDto(savedCollectionArt);
    }
    
    // Удаление арта из коллекции
    public void removeArtFromCollection(Long collectionId, Long artId) {
        log.info("Removing art {} from collection {}", artId, collectionId);
        
        if (!collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId)) {
            log.warn("Art {} not found in collection {}", artId, collectionId);
            return; // Или можно выбросить исключение
        }
        
        collectionArtRepository.deleteByCollectionIdAndArtId(collectionId, artId);
        log.info("Successfully removed art {} from collection {}", artId, collectionId);
    }
    
    // Удаление всех артов из коллекции
    public void removeAllArtsFromCollection(Long collectionId) {
        log.info("Removing all arts from collection {}", collectionId);
        
        Long count = collectionArtRepository.countByCollectionId(collectionId);
        collectionArtRepository.deleteByCollectionId(collectionId);
        
        log.info("Removed {} arts from collection {}", count, collectionId);
    }
    
    // Получение артов коллекции
    @Transactional(readOnly = true)
    public List<ArtDto> getArtsByCollectionId(Long collectionId) {
        log.debug("Getting arts for collection {}", collectionId);
        
        List<CollectionArt> collectionArts = collectionArtRepository.findByCollectionId(collectionId);
        
        if (collectionArts == null || collectionArts.isEmpty()) {
            log.debug("No arts found for collection {}", collectionId);
            return Collections.emptyList();
        }
        
        List<ArtDto> arts = collectionArts.stream()
                .map(CollectionArt::getArt)
                .filter(art -> art != null)
                .map(art -> {
                    try {
                        return artService.getArtDtoById(art.getId()).orElse(null);
                    } catch (Exception e) {
                        log.warn("Error loading art {}: {}", art.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(artDto -> artDto != null)
                .collect(Collectors.toList());
        
        log.debug("Found {} arts for collection {}", arts.size(), collectionId);
        return arts;
    }
    
    // Получение коллекций арта
    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionsByArtId(Long artId) {
        log.debug("Getting collections for art {}", artId);
        
        List<CollectionArt> collectionArts = collectionArtRepository.findByArtId(artId);
        List<CollectionArtDto> result = collectionArts.stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
        
        log.debug("Found {} collections for art {}", result.size(), artId);
        return result;
    }
    
    // Получение CollectionArtDto для коллекции
    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionArtDtosByCollectionId(Long collectionId) {
        log.debug("Getting CollectionArtDto for collection {}", collectionId);
        
        List<CollectionArt> collectionArts = collectionArtRepository.findByCollectionId(collectionId);
        List<CollectionArtDto> result = collectionArts.stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
        
        log.debug("Found {} CollectionArtDto for collection {}", result.size(), collectionId);
        return result;
    }
    
    // Количество артов в коллекции
    @Transactional(readOnly = true)
    public Long getArtCountByCollectionId(Long collectionId) {
        log.debug("Getting art count for collection {}", collectionId);
        
        Long count = collectionArtRepository.countByCollectionId(collectionId);
        log.debug("Collection {} has {} arts", collectionId, count);
        return count;
    }
    
    // Проверка существования связи
    @Transactional(readOnly = true)
    public boolean existsByCollectionIdAndArtId(Long collectionId, Long artId) {
        return collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId);
    }
    
    // Перемещение арта между коллекциями
    public void moveArtBetweenCollections(Long artId, Long fromCollectionId, Long toCollectionId) {
        log.info("Moving art {} from collection {} to collection {}", 
                 artId, fromCollectionId, toCollectionId);
        
        if (fromCollectionId.equals(toCollectionId)) {
            throw new RuntimeException("Source and target collections are the same");
        }
        
        // Проверяем, существует ли арт в исходной коллекции
        if (!collectionArtRepository.existsByCollectionIdAndArtId(fromCollectionId, artId)) {
            throw new RuntimeException("Art not found in source collection");
        }
        
        // Проверяем, не существует ли уже в целевой коллекции
        if (collectionArtRepository.existsByCollectionIdAndArtId(toCollectionId, artId)) {
            throw new RuntimeException("Art already exists in target collection");
        }
        
        // Удаляем из исходной коллекции
        collectionArtRepository.deleteByCollectionIdAndArtId(fromCollectionId, artId);
        
        // Добавляем в целевую коллекцию
        addArtToCollection(toCollectionId, artId);
        
        log.info("Successfully moved art {} from collection {} to collection {}", 
                 artId, fromCollectionId, toCollectionId);
    }
    
    // Копирование арта в коллекцию
    public CollectionArtDto copyArtToCollection(Long artId, Long collectionId) {
        log.info("Copying art {} to collection {}", artId, collectionId);
        return addArtToCollection(collectionId, artId);
    }
}