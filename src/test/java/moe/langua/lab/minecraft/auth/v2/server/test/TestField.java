package moe.langua.lab.minecraft.auth.v2.server.test;

import moe.langua.lab.minecraft.auth.v2.server.sql.DataSearcher;
import moe.langua.lab.minecraft.auth.v2.server.sql.SQLiteDataSearcher;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestField {
    public static void main(String[] args) throws SQLException, UnknownHostException, InterruptedException {
        DataSearcher searcher = new SQLiteDataSearcher(new File(new File("").getAbsolutePath()), "AuthV2_");
        AtomicInteger i = new AtomicInteger();
        new Thread(() -> {
            int last = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Operations per second: " + (i.get() - last));
                last = i.get();
            }
        }).start();

        UUID uniqueID;
        InetAddress address = InetAddress.getLocalHost();
        while (true) {
            uniqueID = UUID.randomUUID();
            searcher.getPlayerStatus(uniqueID);
            searcher.setPlayerStatus(uniqueID, true, address);
            i.addAndGet(1);
        }
    }
}
