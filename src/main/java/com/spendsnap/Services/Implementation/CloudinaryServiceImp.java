package com.spendsnap.Services.Implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spendsnap.Services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImp implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String categoryIconUpload(MultipartFile iconFile) {
        try{
            Map uploadResult = cloudinary.uploader().upload(
                    iconFile.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "CategoryIcons"   // this tells Cloudinary to store inside your folder
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String uploadPicture(MultipartFile userPic) {
        try{
            Map uploadResult = cloudinary.uploader().upload(
                    userPic.getBytes(),
                    ObjectUtils.asMap("folder","userProfile")
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Void deleteDataImage(String secure_url) throws IOException {
         String public_id = extractPublicId(secure_url);
         cloudinary.uploader().destroy(public_id,ObjectUtils.emptyMap());
        return null;
    }


    // Extract publicId from secureUrl
    private String extractPublicId(String secureUrl) {
        // Example: https://res.cloudinary.com/.../upload/v1234567890/userProfile/glhfsdonyw4en9ht4de1.jpg
        String path = secureUrl.substring(secureUrl.indexOf("/upload/") + 8); // get after /upload/
        // Remove version part (e.g., v1756015987/)
        int slashIndex = path.indexOf('/');
        path = path.substring(slashIndex + 1);
        // Remove extension
        return path.substring(0, path.lastIndexOf('.'));
    }

}