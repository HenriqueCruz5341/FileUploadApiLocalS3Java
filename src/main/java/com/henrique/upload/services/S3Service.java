package com.henrique.upload.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.henrique.upload.documents.FileDocument;
import com.henrique.upload.domains.S3;
import com.henrique.upload.models.FileDeleteDto;
import com.henrique.upload.models.FileResponseDto;
import com.henrique.upload.repositories.FileRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class S3Service implements S3 {

    private final FileRepository fileRepository;
    private AmazonS3 s3client;
    private final String endpointUrl;
    private final String bucketName;
    private final String accessKey;
    private final String secretKey;

    public S3Service(FileRepository fileRepository, @Value("${amazonProperties.endpointUrl}") String endpointUrl,
            @Value("${amazonProperties.bucketName}") String bucketName,
            @Value("${amazonProperties.accessKey}") String accessKey,
            @Value("${amazonProperties.secretKey}") String secretKey) {
        this.fileRepository = fileRepository;
        this.endpointUrl = endpointUrl;
        this.bucketName = bucketName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    @PostConstruct
    public void initializeAmazon() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_2)
                .build();
    }

    @Override
    public List<FileResponseDto> getAll(Integer pageIndex, Integer pageSize) {
        Page<FileDocument> fileDocumentPage = fileRepository.findAllS3(PageRequest.of(pageIndex, pageSize));

        return fileDocumentPage.toList().stream().map(FileResponseDto::new).collect(Collectors.toList());
    }

    @Override
    public FileResponseDto getByName(String name) {
        Optional<FileDocument> fileDocumentOptional = fileRepository.findByName(name);

        if (fileDocumentOptional.isPresent()) {
            FileDocument fileDocument = fileDocumentOptional.get();
            return new FileResponseDto(fileDocument);
        }

        return null;
    }

    @Override
    public FileResponseDto upload(MultipartFile multipartFile) {
        String fileUrl = "";

        try {
            File file = convertMultiPartToFile(multipartFile);
            String newFileName = normalize(UUID.randomUUID().toString().substring(24) + "-"
                    + multipartFile.getOriginalFilename());
            newFileName = newFileName.replace(" ", "_");
            multipartFile.transferTo(file);

            fileUrl = endpointUrl + "/" + bucketName + "/" + newFileName;

            FileDocument fileDocument = new FileDocument();
            fileDocument.setName(newFileName);
            fileDocument.setDownloadUri(fileUrl);
            fileDocument.setType(multipartFile.getContentType());
            fileDocument.setSize(multipartFile.getSize());
            fileDocument.setLocation("s3");

            s3client.putObject(new PutObjectRequest(bucketName, newFileName, file)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            return new FileResponseDto(fileRepository.save(fileDocument));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Could not store file " + multipartFile.getOriginalFilename() + ". Please try again!");
        }

    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
            return convertedFile;
        } catch (FileNotFoundException e) {
            throw new IOException("Error converting file");
        }
    }

    private String normalize(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public FileDeleteDto delete(String name) {
        Optional<FileDocument> fileDocumentOptional = fileRepository.findByName(name);
        if (fileDocumentOptional.isPresent()) {
            FileDocument fileDocument = fileDocumentOptional.get();
            s3client.deleteObject(new DeleteObjectRequest(bucketName, fileDocument.getName()));
            fileRepository.delete(fileDocument);

            return new FileDeleteDto("Arquivo deletado com sucesso!", HttpStatus.OK.value());
        } else {
            return new FileDeleteDto("Arquivo não encontrado", HttpStatus.NOT_FOUND.value());
        }

    }

}
