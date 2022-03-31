package com.henrique.upload.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.henrique.upload.documents.FileDocument;
import com.henrique.upload.domains.Local;
import com.henrique.upload.models.FileDownloadDto;
import com.henrique.upload.models.FileResponseDto;
import com.henrique.upload.repositories.FileRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalService implements Local {

    private final FileRepository fileRepository;
    private static final String UPLOAD_PATH = "/home/henrique/elogroup/desenv/sda-sp/upload-files/";
    private static final String DOWNLOAD_URL = "http://localhost:8080/local/";

    public LocalService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public List<FileResponseDto> getAll(Integer pageIndex, Integer pageSize) {
        Page<FileDocument> fileDocumentPage = fileRepository.findAllNotS3(PageRequest.of(pageIndex, pageSize));

        return fileDocumentPage.toList().stream().map(FileResponseDto::new).collect(Collectors.toList());
    }

    @Override
    public FileDownloadDto download(String fileName) {
        Optional<FileDocument> fileDocumentOptional = fileRepository.findByName(fileName);

        try {
            FileDownloadDto fileDownloadDto = new FileDownloadDto();
            if (fileDocumentOptional.isPresent()) {
                FileDocument fileDocument = fileDocumentOptional.get();
                fileDownloadDto.setName(fileDocument.getName());
                fileDownloadDto.setSize(fileDocument.getSize());
                fileDownloadDto.setType(fileDocument.getType());
                Path path = Paths.get(UPLOAD_PATH, fileName);
                fileDownloadDto.setContent(Files.readAllBytes(path));
            }

            return fileDownloadDto;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao baixar arquivo");
        }
    }

    @Override
    public FileResponseDto upload(MultipartFile file) {
        Path diretorioPath = Paths.get(UPLOAD_PATH);

        try {
            Files.createDirectories(diretorioPath);

            String newFileName = UUID.randomUUID().toString().substring(24) + "-"
                    + file.getOriginalFilename();
            newFileName = newFileName.replace(" ", "_");
            File newFile = new File(UPLOAD_PATH + newFileName);
            file.transferTo(newFile);

            FileDocument fileDocument = new FileDocument();
            fileDocument.setName(newFileName);
            fileDocument.setDownloadUri(DOWNLOAD_URL + newFileName);
            fileDocument.setType(file.getContentType());
            fileDocument.setSize(file.getSize());
            fileDocument.setLocation(UPLOAD_PATH + newFileName);

            return new FileResponseDto(fileRepository.save(fileDocument));
        } catch (IOException e) {
            throw new RuntimeException("Problemas na tentativa de salvar arquivo.", e);
        }
    }

}
