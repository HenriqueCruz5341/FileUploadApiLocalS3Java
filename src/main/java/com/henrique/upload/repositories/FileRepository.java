package com.henrique.upload.repositories;

import java.util.Optional;

import com.henrique.upload.documents.FileDocument;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends PagingAndSortingRepository<FileDocument, String> {

    @Query("{'name': ?0}")
    Optional<FileDocument> findByName(String name);

    @Query("{location: {'$ne': 's3' } }")
    Page<FileDocument> findAllNotS3(PageRequest pageRequest);

    @Query("{'location': 's3'}")
    Page<FileDocument> findAllS3(PageRequest pageRequest);
}
