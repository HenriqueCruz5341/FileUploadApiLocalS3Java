package com.henrique.upload.models;

import lombok.Data;

@Data
public class FileDownloadDto {
    private byte[] content;
    private String type;
    private String name;
    private Long size;
}
