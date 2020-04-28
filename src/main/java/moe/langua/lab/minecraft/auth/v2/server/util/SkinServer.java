package moe.langua.lab.minecraft.auth.v2.server.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SkinServer {
    private final File dataRoot;
    private final String host;
    private final long expireTime;

    public SkinServer(File dataRoot, String host, long expireTime) {
        if (!dataRoot.exists()) dataRoot.mkdir();
        if (!dataRoot.isDirectory()) {
            System.exit(-3);
        }
        this.dataRoot = dataRoot;
        this.host = host;
        this.expireTime = expireTime;
    }

    public File getDataRoot() {
        return dataRoot;
    }


    public String putSkin(BufferedImage skinImage) throws IOException {
        String fileName = Integer.toHexString(skinImage.hashCode()) + ".png";
        ImageIO.write(skinImage, "png", new File(dataRoot, fileName));
        return host + "/get/skin/" + fileName;
    }

    public void purge() {
        purge(expireTime);
    }


    private void purge(long expireTime) {
        long now = System.currentTimeMillis();
        for (File x : Objects.requireNonNull(dataRoot.listFiles())) {
            if (now - x.lastModified() > expireTime) {
                x.delete();
            }
        }
    }

    public void purgeAll() {
        purge(-1);
    }
}
