package moe.langua.lab.minecraft.auth.v2.server.util;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import moe.langua.lab.minecraft.auth.v2.server.json.mojang.PlayerProfile;
import moe.langua.lab.minecraft.auth.v2.server.json.mojang.Profile;
import moe.langua.lab.minecraft.auth.v2.server.json.mojang.Property;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Config;
import moe.langua.lab.minecraft.auth.v2.server.json.server.Error;
import moe.langua.lab.utils.logger.MelonLogger;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Utils {
    public static final int[] colorTable = {0xff032230, 0xff053248, 0xff07435f, 0xff085477, 0xff0a658f, 0xff0c76a7, 0xff0d87bf, 0xff11a8ee, 0xff40baf2, 0xff70cbf5, 0xff88d4f7, 0xffa0dcf8, 0xffb7e5fa, 0xffcfeefc, 0xffe7f6fd, 0xffffffff};
    public static final Gson gson = new Gson();
    public static final MelonLogger logger = MelonLogger.getLogger();
    private static final Random random = new Random();
    private static BufferedImage STEVE;
    private static BufferedImage ALEX;

    static {
        try {
            STEVE = ImageIO.read(Utils.class.getResource("/steve.png"));
            ALEX = ImageIO.read(Utils.class.getResource("/alex.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void paintVerificationCode(BufferedImage targetSkin, int[] verificationCode) {
        int colorIndex;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                colorIndex = verificationCode[(y * 8) + x];
                targetSkin.setRGB(x, y, colorTable[colorIndex]);
            }
        }
    }

    public static byte[] getPNGImageBytes(BufferedImage targetSkin) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(targetSkin, "png", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static BufferedImage getSkin(UUID uniqueID) throws IOException {
        Profile profile = getPlayerProfile(uniqueID);
        return getSkinFromProfile(profile);
    }

    public static Profile getPlayerProfile(UUID uniqueID) throws IOException {
        URL reqURL = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uniqueID.toString().replace("-", ""));
        String result = urlGet(reqURL);
        return gson.fromJson(result, Profile.class);
    }

    public static String getPlayerSkinModel(Profile profile) {
        Property property = profile.properties.get(0);
        String playerProfileJSON = base64ToString(property.value);
        PlayerProfile playerProfile = gson.fromJson(playerProfileJSON, PlayerProfile.class);
        return playerProfile.textures.sKIN.metadata == null ? "steve" : "alex";
    }

    public static BufferedImage getSkinFromProfile(Profile profile) throws IOException {
        Property property = profile.properties.get(0);
        String playerProfileJSON = base64ToString(property.value);
        PlayerProfile playerProfile = gson.fromJson(playerProfileJSON, PlayerProfile.class);
        if (playerProfile.textures.sKIN == null) {
            return (fullUUIDFromTrimmed(profile.id).hashCode() & 1) == 0 ? STEVE : ALEX;
        } else {
            return ImageIO.read(new URL(playerProfile.textures.sKIN.url.replace("http://", "https://")));
        }
    }

    private static String urlGet(URL reqURL) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) reqURL.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "curl/7.58.0");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(false);
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String tmpString;
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            tmpString = bufferedReader.readLine();
            if (tmpString == null) break;
            stringBuilder.append(tmpString);
        }
        return stringBuilder.toString();
    }

    public static String toBase64(String target) {
        return Base64.getEncoder().encodeToString(target.getBytes());
    }

    public static String base64ToString(String target) {
        return new String(Base64.getDecoder().decode(target));
    }

    public static String getLastChild(URI targetUri) {
        String urlString = targetUri.toString();
        return urlString.substring(urlString.lastIndexOf('/') + 1);
    }

    public static int[] generateRandomVerificationCodeArray() {
        int[] result = new int[64];
        for (int index = 0; index < result.length; index++) {
            result[index] = random.nextInt(16);
        }
        return result;
    }

    public static String appendNewLineIfNotEndWithANewLine(String target) {
        return target.endsWith("\n") ? target : target.concat("\n");
    }

    public static UUID fullUUIDFromTrimmed(String trimmedUUID) throws IllegalArgumentException {
        if (trimmedUUID == null) throw new IllegalArgumentException();
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        /* Backwards adding to avoid index adjustments */
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }
        return UUID.fromString(builder.toString());
    }

    public static String removeSlashAtTheEnd(String target) {
        while (target.endsWith("/")) {
            target = target.substring(0, target.length() - 1);
        }
        return target;
    }

    public static class server {
        public static final Error NOT_FOUND_ERROR = new Error("Not Found", "The server has not found anything matching the request URI", null);
        public static final Error TOO_MANY_REQUEST_ERROR = new Error("TooManyRequestsException", "The client has sent too many requests within a certain amount of time", null);
        //public static final Error VERIFICATION_EXPIRED_ERROR = new Error("Verification Expired", "Your Verification has been expired, please reconnect to server and get a new verification code", null);
        public static final Error SERVER_NETWORK_ERROR = new Error("Server Network Error", "Server network communication error, please try again later", null);
        public static final Error INTERNAL_ERROR = new Error("Internal Server Error", "Internal error occurred", null);
        public static final Error VERIFICATION_FAILED = new Error("Verification Failed", "Wrong skin pixel detected, please check your skin upload and try again", null);

        public static void errorReturn(HttpExchange httpExchange, int responseCode, Error error) {
            writeJSONAndSend(httpExchange, responseCode, gson.toJson(error));
        }

        public static void writeJSONAndSend(HttpExchange httpExchange, int responseCode, String content) {
            String response = appendNewLineIfNotEndWithANewLine(content);
            writeAndSend(httpExchange, responseCode, "application/json", response.getBytes(UTF_8), response.length());
        }

        public static void writeAndSend(HttpExchange httpExchange, int responseCode, String contentType, byte[] content, long contentLength) {
            try {
                setCORSHeader(httpExchange);
                httpExchange.getResponseHeaders().set("Content-Type", contentType);
                httpExchange.sendResponseHeaders(responseCode, contentLength);
                httpExchange.getResponseBody().write(content);
                httpExchange.getResponseBody().flush();
                httpExchange.getResponseBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public static void returnNoContent(HttpExchange httpExchange, int rCode) {
            try {
                setCORSHeader(httpExchange);
                httpExchange.sendResponseHeaders(rCode, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private static void setCORSHeader(HttpExchange httpExchange) {
            if (!httpExchange.getRequestHeaders().containsKey("Origin")) return;
            String origin = removeSlashAtTheEnd(httpExchange.getRequestHeaders().getFirst("Origin"));
            if (Config.instance.CORSList.contains(origin))
                httpExchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        }
    }


}
