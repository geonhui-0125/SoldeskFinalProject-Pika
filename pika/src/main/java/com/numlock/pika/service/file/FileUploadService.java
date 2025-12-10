package com.numlock.pika.service.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    //application.properties에 설정한 업로드 경로
    private final String uploadDir = "src/main/resources/static/profile/";

    public String store(MultipartFile multipartFile) throws IOException {
        if(multipartFile.isEmpty()){
            return null;
            //업로드 파일 없을 경우 기본 이미지 반환 혹은 예외처리,
            //컨트롤러에서 기본 이미지 경로 설정 필요
        }

        Path uploadPath = Paths.get(uploadDir);

        //업로드 디렉토리가 존재하지 않으면 생성
        if(!Files.exists(uploadPath)){
            Files.createDirectories(uploadPath);
        }

        //중복명 피하기 위해 새로운 파일명 생성
        String originFilename = multipartFile.getOriginalFilename();
        String fileExtension = "";
        if(originFilename != null && originFilename.contains(".")) {
            fileExtension = originFilename.substring(originFilename.lastIndexOf("."));
        }
        String storedFileName = UUID.randomUUID().toString() + fileExtension;

        //파일 최종 저장 경로
        Path destinationFile = uploadPath.resolve(storedFileName);

        //파일 저장
        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        //웹에서 접근할 수 있는 경로
        return "/profile/" + storedFileName;

    }

}
