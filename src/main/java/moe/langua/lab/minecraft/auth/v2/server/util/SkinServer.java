package moe.langua.lab.minecraft.auth.v2.server.util;

import moe.langua.lab.utils.logger.utils.LogRecord;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SkinServer {
    private final File dataRoot;
    private final long expireTime;

    public SkinServer(File dataRoot, long expireTime) {
        if (!dataRoot.mkdir() && !dataRoot.isDirectory()) {
            Utils.logger.log(LogRecord.Level.ERROR, new IOException("LogFolder " + dataRoot.getAbsolutePath() + " should be a folder, but found a file.").toString());
        }
        this.dataRoot = dataRoot;
        this.expireTime = expireTime;
    }

    public File getDataRoot() {
        return dataRoot;
    }


    public String putSkin(BufferedImage skinImage) throws IOException {
        String fileName = Integer.toHexString(skinImage.hashCode());
        File skinFile = new File(dataRoot, fileName + ".png");
        long differ = 0;
        while (skinFile.exists()) {
            skinFile = new File(dataRoot, fileName + Long.toHexString(differ++) + ".png");
        }
        Utils.logger.debug("Finale skin file: " + skinFile.getAbsolutePath());
        ImageIO.write(skinImage, "png", skinFile);
        return "/get/skin/" + skinFile.getName();
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
