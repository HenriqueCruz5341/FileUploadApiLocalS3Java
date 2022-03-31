package com.henrique.upload.models;

import com.henrique.upload.documents.FileDocument;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileResponseDto {

    private String name;
    private String downloadUri;
    private String location;
    private String type;
    private Long size;

    public FileResponseDto(FileDocument fileDocument) {
        this.name = fileDocument.getName();
        this.downloadUri = fileDocument.getDownloadUri();
        this.location = fileDocument.getLocation();
        this.type = fileDocument.getType();
        this.size = fileDocument.getSize();
    }

}
