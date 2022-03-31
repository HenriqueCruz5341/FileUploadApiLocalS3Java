package com.henrique.upload.domains;

import java.util.List;

import com.henrique.upload.models.FileDeleteDto;
import com.henrique.upload.models.FileResponseDto;

import org.springframework.web.multipart.MultipartFile;

public interface S3 {

    void initializeAmazon();

    FileResponseDto getByName(String name);

    List<FileResponseDto> getAll(Integer pageIndex, Integer pageSize);

    FileResponseDto upload(MultipartFile multipartFile);

    FileDeleteDto delete(String name);

}
