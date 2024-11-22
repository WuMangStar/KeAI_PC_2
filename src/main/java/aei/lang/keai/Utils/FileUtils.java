package aei.lang.keai.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static void createCacheDirectory(String CACHE_DIR) {
        File cacheDir = new File(CACHE_DIR);
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("无法创建缓存目录: " + CACHE_DIR);
        }
    }

    public static void saveByte(byte[] Bytes, File saveFile){
        try (FileOutputStream fos = new FileOutputStream(saveFile)) {
            fos.write(Bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
