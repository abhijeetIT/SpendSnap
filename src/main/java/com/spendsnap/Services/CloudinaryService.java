package com.spendsnap.Services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
      String categoryIconUpload(MultipartFile userPic);
      String uploadPicture(MultipartFile iconFile);
      Void deleteDataImage(String secure_url) throws IOException;
}
