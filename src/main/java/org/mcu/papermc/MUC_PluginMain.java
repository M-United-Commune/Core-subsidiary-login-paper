package org.mcu.papermc;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class MUC_PluginMain extends JavaPlugin implements Listener {
    public static class Response_Message {
        int code;
        String message;

        public Response_Message(String message) {
            this.code = 400;
            this.message = message;
        }
    }
 
    //    临时在线玩家列表
    HashMap<String, String> player_map = new HashMap<>();
    String context_uri = "http://10980xe.mc5173.cn:10124/api";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        this.getLogger().info("开始加载MUC-paper端-附属登录插件");
        String uri = "http://10980xe.mc5173.cn:10124/api";
//        加载配置文件
        if (getConfig().contains("uri")) {
            context_uri = getConfig().getString("uri");
        } else {
            getConfig().set("uri", uri);
            saveConfig();
        }

        getCommand("login").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                Response_Message response = new Response_Message("please login first");
                if (args.length == 1) {
                    player.sendMessage("登录账号/login <password>");
                    var password = args[0];
                    response = doGet_auth(context_uri, password);
                    if (response.code == 200) {
                        player_map.put(player.getName(), player.getAddress().getHostName());
                        player.sendMessage("登录账号成功");
                    }
                } else if (args.length == 2) {
                    player.sendMessage("绑定账号/login <password> <token>");
                    var token = args[1];
                    var password = args[0];
                    response = doPost_bind_player(context_uri, token, player.getName(), password);
                    if (response.code == 200) {
                        player_map.put(player.getName(), player.getAddress().getHostName());
                        player.sendMessage("绑定账号成功");
                    }
                }
                if (response.code != 200) {
                    player.sendMessage("非法错误");
                }
            } else {
                sender.sendMessage("你不是玩家!");
            }
            return true;
        });
    }

    public Response_Message doGet_auth(String uri, String password) {
        try {
            HttpURLConnection urlConnection;
            String url = uri + "/java/player/login?password=" + password;
            URL urlObject = new URL(url);
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(8000);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            String responseMessage = "";
            if (code == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    responseMessage += line;
                }
                reader.close();
                urlConnection.disconnect();
                Gson gson = new Gson();
                return gson.fromJson(responseMessage, Response_Message.class);
            } else {
                return new Response_Message("登录失败");
            }
        } catch (IOException e) {
            return new Response_Message("登录失败，请稍后再试");
        }
    }

    public Response_Message doPost_bind_player(String uri, String token, String player_name, String password) {
        try {
            HttpURLConnection urlConnection;
            String url = uri + "/java/player/bind?player_name=" + player_name + "&password=" + password;
            URL urlObject = new URL(url);
            urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Authorization", token);
            urlConnection.setConnectTimeout(8000);
            urlConnection.setReadTimeout(8000);
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            String responseMessage = "";
            if (code == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    responseMessage += line;
                }
                reader.close();
                urlConnection.disconnect();
                Gson gson = new Gson();
                return gson.fromJson(responseMessage, Response_Message.class);
            }
            return new Response_Message("绑定账户失败，请检查token");
        } catch (IOException e) {
            return new Response_Message("连接失败，请稍后再试");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    //    监听玩家移动
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var player = player_map.get(event.getPlayer().getName());
        if (player == null) {
            event.setCancelled(true);
            String message = "请登录账号/login <password>";
            event.getPlayer().sendMessage(message);
            String message2 = "若无账户，请绑定账号/login <password> <token>";
            event.getPlayer().sendMessage(message2);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!Objects.equals(player_map.get(player.getName()), player.getAddress().getHostName())) {
            player_map.remove(player.getName());
            player.sendMessage("欢迎回来");
        }


    }

}