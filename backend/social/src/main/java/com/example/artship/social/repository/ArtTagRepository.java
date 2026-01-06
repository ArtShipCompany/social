package com.example.artship.social.repository;

import com.example.artship.social.model.ArtTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtTagRepository extends JpaRepository<ArtTag, ArtTag.ArtTagId> {
    
    // Найти все связи по artId
    @Query("SELECT at FROM ArtTag at WHERE at.art.id = :artId")
    List<ArtTag> findByArtId(@Param("artId") Long artId);
    
    // Найти все связи по tagId
    @Query("SELECT at FROM ArtTag at WHERE at.tag.id = :tagId")
    List<ArtTag> findByTagId(@Param("tagId") Long tagId);
    
    // Найти конкретную связь по artId и tagId
    @Query("SELECT at FROM ArtTag at WHERE at.art.id = :artId AND at.tag.id = :tagId")
    Optional<ArtTag> findByArtIdAndTagId(@Param("artId") Long artId, @Param("tagId") Long tagId);
    
    // Найти все связи по artId и tagId (множественные)
    @Query("SELECT at FROM ArtTag at WHERE at.art.id = :artId AND at.tag.id = :tagId")
    List<ArtTag> findAllByArtIdAndTagId(@Param("artId") Long artId, @Param("tagId") Long tagId);
    
    // Проверить существование связи
    @Query("SELECT CASE WHEN COUNT(at) > 0 THEN true ELSE false END " +
           "FROM ArtTag at WHERE at.art.id = :artId AND at.tag.id = :tagId")
    boolean existsByArtIdAndTagId(@Param("artId") Long artId, @Param("tagId") Long tagId);
    
    // Количество связей по artId
    @Query("SELECT COUNT(at) FROM ArtTag at WHERE at.art.id = :artId")
    Long countByArtId(@Param("artId") Long artId);
    
    // Количество связей по tagId
    @Query("SELECT COUNT(at) FROM ArtTag at WHERE at.tag.id = :tagId")
    Long countByTagId(@Param("tagId") Long tagId);
    
    // Количество уникальных тегов у арта
    @Query("SELECT COUNT(DISTINCT at.tag.id) FROM ArtTag at WHERE at.art.id = :artId")
    Long countDistinctTagsByArtId(@Param("artId") Long artId);
    
    // Количество уникальных артов у тега
    @Query("SELECT COUNT(DISTINCT at.art.id) FROM ArtTag at WHERE at.tag.id = :tagId")
    Long countDistinctArtsByTagId(@Param("tagId") Long tagId);
    
    // Удалить связь по artId и tagId
    @Modifying
    @Query("DELETE FROM ArtTag at WHERE at.art.id = :artId AND at.tag.id = :tagId")
    void deleteByArtIdAndTagId(@Param("artId") Long artId, @Param("tagId") Long tagId);
    
    // Удалить все связи по artId
    @Modifying
    @Query("DELETE FROM ArtTag at WHERE at.art.id = :artId")
    void deleteByArtId(@Param("artId") Long artId);
    
    // Удалить все связи по tagId
    @Modifying
    @Query("DELETE FROM ArtTag at WHERE at.tag.id = :tagId")
    void deleteByTagId(@Param("tagId") Long tagId);
    
    // Найти связи по нескольким artId
    @Query("SELECT at FROM ArtTag at WHERE at.art.id IN :artIds")
    List<ArtTag> findByArtIds(@Param("artIds") List<Long> artIds);
    
    // Найти связи по нескольким tagId
    @Query("SELECT at FROM ArtTag at WHERE at.tag.id IN :tagIds")
    List<ArtTag> findByTagIds(@Param("tagIds") List<Long> tagIds);
    
    // Получить самые популярные теги (с наибольшим количеством артов)
    @Query("SELECT at.tag.id, COUNT(at.art.id) as artCount " +
           "FROM ArtTag at GROUP BY at.tag.id ORDER BY artCount DESC")
    List<Object[]> findMostPopularTagIds(@Param("limit") int limit);
    
    // Получить арты с наибольшим количеством тегов
    @Query("SELECT at.art.id, COUNT(at.tag.id) as tagCount " +
           "FROM ArtTag at GROUP BY at.art.id ORDER BY tagCount DESC")
    List<Object[]> findArtsWithMostTags(@Param("limit") int limit);
}