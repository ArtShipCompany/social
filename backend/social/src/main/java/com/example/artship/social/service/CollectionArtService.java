package com.example.artship.social.service;

import com.example.artship.social.dto.ArtDto;
import com.example.artship.social.dto.CollectionArtDto;
import com.example.artship.social.model.Art;
import com.example.artship.social.model.Collection;
import com.example.artship.social.model.CollectionArt;
import com.example.artship.social.repository.ArtRepository;
import com.example.artship.social.repository.CollectionArtRepository;
import com.example.artship.social.repository.CollectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CollectionArtService {
    
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
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));
        Art art = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Art not found with id: " + artId));
        
        // Проверяем, что арт публичный или пользователь является автором
        if (!art.getIsPublic() && !art.getAuthor().getId().equals(collection.getUser().getId())) {
            throw new RuntimeException("Cannot add private art to collection");
        }
        
        if (collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId)) {
            throw new RuntimeException("Art already exists in collection");
        }
        
        CollectionArt collectionArt = new CollectionArt(collection, art);
        CollectionArt savedCollectionArt = collectionArtRepository.save(collectionArt);
        return new CollectionArtDto(savedCollectionArt);
    }
    
    // Удаление арта из коллекции
    public void removeArtFromCollection(Long collectionId, Long artId) {
        collectionArtRepository.deleteByCollectionIdAndArtId(collectionId, artId);
    }
    
    // Удаление всех артов из коллекции
    public void removeAllArtsFromCollection(Long collectionId) {
        collectionArtRepository.deleteByCollectionId(collectionId);
    }
    
    // Получение артов коллекции
    @Transactional(readOnly = true)
    public List<ArtDto> getArtsByCollectionId(Long collectionId) {
        return collectionArtRepository.findByCollectionId(collectionId).stream()
                .map(CollectionArt::getArt)
                .map(art -> artService.getArtDtoById(art.getId()).orElse(null))
                .filter(artDto -> artDto != null)
                .collect(Collectors.toList());
    }
    
    // Получение коллекций арта
    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionsByArtId(Long artId) {
        return collectionArtRepository.findByArtId(artId).stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
    }
    
    // Получение CollectionArtDto для коллекции
    @Transactional(readOnly = true)
    public List<CollectionArtDto> getCollectionArtDtosByCollectionId(Long collectionId) {
        return collectionArtRepository.findByCollectionId(collectionId).stream()
                .map(CollectionArtDto::new)
                .collect(Collectors.toList());
    }
    
    // Количество артов в коллекции
    @Transactional(readOnly = true)
    public Long getArtCountByCollectionId(Long collectionId) {
        return collectionArtRepository.countByCollectionId(collectionId);
    }
    
    // Проверка существования связи
    @Transactional(readOnly = true)
    public boolean existsByCollectionIdAndArtId(Long collectionId, Long artId) {
        return collectionArtRepository.existsByCollectionIdAndArtId(collectionId, artId);
    }
    
    // Перемещение арта между коллекциями
    public void moveArtBetweenCollections(Long artId, Long fromCollectionId, Long toCollectionId) {
        if (fromCollectionId.equals(toCollectionId)) {
            throw new RuntimeException("Source and target collections are the same");
        }
        
        // Удаляем из исходной коллекции
        collectionArtRepository.deleteByCollectionIdAndArtId(fromCollectionId, artId);
        
        // Добавляем в целевую коллекцию
        addArtToCollection(toCollectionId, artId);
    }
    
    // Копирование арта в коллекцию
    public CollectionArtDto copyArtToCollection(Long artId, Long collectionId) {
        return addArtToCollection(collectionId, artId);
    }
}