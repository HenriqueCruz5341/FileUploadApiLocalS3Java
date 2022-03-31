package com.henrique.upload.domains;

import java.util.List;

import com.henrique.upload.models.FileDownloadDto;
import com.henrique.upload.models.FileResponseDto;

import org.springframework.web.multipart.MultipartFile;

public interface Local {

    List<FileResponseDto> getAll(Integer pageIndex, Integer pageSize);

    FileDownloadDto download(String fileName);

    FileResponseDto upload(MultipartFile file);

}
