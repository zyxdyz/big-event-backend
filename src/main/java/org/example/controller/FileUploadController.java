package org.example.controller;

import org.example.pojo.Result;
import org.example.utils.AliOssUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
public class FileUploadController {
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        String objectName = file.getOriginalFilename();
        String url=AliOssUtil.uploadFile(String.valueOf(UUID.randomUUID())
                        +objectName.substring(objectName.lastIndexOf('.')),
                file.getInputStream());
        return Result.success(url);
        /*file.transferTo(new File("D:\\Program Files (x86)\\Java\\temp\\" + objectName));
        String url="D:\\Program Files (x86)\\Java\\temp\\" + objectName;
        return Result.success(url);*/

    }

}
