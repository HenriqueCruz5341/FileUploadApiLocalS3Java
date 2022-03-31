package com.henrique.upload.controllers;

import java.util.List;

import com.henrique.upload.domains.Local;
import com.henrique.upload.models.FileDownloadDto;
import com.henrique.upload.models.FileResponseDto;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/local")
public class LocalController {

    private final Local localService;

    public LocalController(Local localService) {
        this.localService = localService;
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDto>> getAll(@RequestParam Integer pageIndex,
            @RequestParam Integer pageSize) {
        List<FileResponseDto> listFileResponseDto = localService.getAll(pageIndex, pageSize);

        return ResponseEntity.status(200).body(listFileResponseDto);
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        FileDownloadDto fileDownloadDto = localService.download(fileName);

        HttpHeaders headers = new HttpHeaders();
        ContentDisposition contentDisposition = ContentDisposition.builder("inline")
                .filename(fileDownloadDto.getName())
                .build();
        headers.setContentDisposition(contentDisposition);
        headers.add(HttpHeaders.CONTENT_TYPE, fileDownloadDto.getType());
        headers.setContentLength(fileDownloadDto.getSize());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(fileDownloadDto.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FileResponseDto> upload(@RequestParam("file") MultipartFile file) {
        FileResponseDto fileResponseDto = localService.upload(file);

        return ResponseEntity.status(201).body(fileResponseDto);
    }

}
