package com.henrique.upload.controllers;

import java.util.List;

import com.henrique.upload.domains.S3;
import com.henrique.upload.models.FileDeleteDto;
import com.henrique.upload.models.FileResponseDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/s3")
public class S3Controller {

    private final S3 s3Service;

    public S3Controller(S3 s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping
    public ResponseEntity<List<FileResponseDto>> getAll(@RequestParam Integer pageIndex,
            @RequestParam Integer pageSize) {
        List<FileResponseDto> listFileResponseDto = s3Service.getAll(pageIndex, pageSize);

        return ResponseEntity.status(200).body(listFileResponseDto);
    }

    @GetMapping(value = "/{name}")
    public RedirectView download(@PathVariable String name) {
        FileResponseDto fileResponseDto = s3Service.getByName(name);

        return new RedirectView(fileResponseDto.getDownloadUri());
    }

    @PostMapping
    public ResponseEntity<FileResponseDto> upload(@RequestPart(value = "file") MultipartFile multipartFile) {
        return ResponseEntity.ok(s3Service.upload(multipartFile));
    }

    @DeleteMapping(value = "/{name}")
    public ResponseEntity<String> delete(@PathVariable String name) {
        FileDeleteDto fileDeleteDto = s3Service.delete(name);

        return ResponseEntity.status(fileDeleteDto.getStatus()).body(fileDeleteDto.getMessage());
    }

}
