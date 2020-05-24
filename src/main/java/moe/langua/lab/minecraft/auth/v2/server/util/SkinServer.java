package moe.langua.lab.minecraft.auth.v2.server.util;

import moe.langua.lab.utils.logger.utils.LogRecord;

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
        if (!dataRoot.mkdir() && !dataRoot.isDirectory()) {
            Utils.logger.log(LogRecord.Level.ERROR, new IOException("LogFolder " + dataRoot.getAbsolutePath() + " should be a folder, but found a file.").toString());
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
