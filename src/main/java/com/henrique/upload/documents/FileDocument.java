package com.henrique.upload.documents;

import com.henrique.upload.models.FileResponseDto;

import org.javers.core.metamodel.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document
public class FileDocument {

    @Id
    private String id;
    private String name;
    private String downloadUri;
    private String location;
    private String type;
    private Long size;

    public FileDocument(FileResponseDto fileResponseDto) {
        this.name = fileResponseDto.getName();
        this.downloadUri = fileResponseDto.getDownloadUri();
        this.location = fileResponseDto.getLocation();
        this.type = fileResponseDto.getType();
        this.size = fileResponseDto.getSize();
    }

}
