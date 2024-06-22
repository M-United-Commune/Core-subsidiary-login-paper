// -*- coding: UTF-8 -*-
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

public class McuLoginPlugin extends JavaPlugin implements Listener {
    public static class Response_Message {
        int code;
        String message;

        public Response_Message(String message) {
            this.code = 400;
            this.message = message;
        }
    }

    //    ¡Ÿ ±‘⁄œﬂÕÊº“¡–±Ì
    HashMap<String, String> player_map = new HashMap<>();
    String context_uri = "http://10980xe.mc5173.cn:10124/api";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        String uri = "http://10980xe.mc5173.cn:10124/api";
//        º”‘ÿ≈‰÷√Œƒº˛
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
                    player.sendMessage("µ«¬º’À∫≈/login <password>");
                    var password = args[0];
                    response = doGet_auth(context_uri, password);
                    if (response.code == 200) {
                        player_map.put(player.getName(), player.getAddress().getHostName());
                        player.sendMessage("µ«¬º’À∫≈≥…π¶");
                    }
                } else if (args.length == 2) {
                    player.sendMessage("∞Û∂®’À∫≈/login <password> <token>");
                    var token = args[1];
                    var password = args[0];
                    response = doPost_bind_player(context_uri, token, player.getName(), password);
                    if (response.code == 200) {
                        player_map.put(player.getName(), player.getAddress().getHostName());
                        player.sendMessage("∞Û∂®’À∫≈≥…π¶");
                    }
                }
                if (response.code != 200) {
                    player.sendMessage("∑«∑®¥ÌŒÛ");
                }
            } else {
                sender.sendMessage("ƒ„≤ª «ÕÊº“!");
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
                return new Response_Message("µ«¬º ß∞‹");
            }
        } catch (IOException e) {
            return new Response_Message("µ«¬º ß∞‹£¨«Î…‘∫Û‘Ÿ ‘");
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
            return new Response_Message("∞Û∂®’Àªß ß∞‹£¨«ÎºÏ≤Ètoken");
        } catch (IOException e) {
            return new Response_Message("¡¨Ω” ß∞‹£¨«Î…‘∫Û‘Ÿ ‘");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    //    º‡Ã˝ÕÊº““∆∂Ø
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var player = player_map.get(event.getPlayer().getName());
        if (player == null) {
            event.setCancelled(true);
            String message = "«Îµ«¬º’À∫≈/login <password>";
            event.getPlayer().sendMessage(message);
            String message2 = "»ÙŒﬁ’Àªß£¨«Î∞Û∂®’À∫≈/login <password> <token>";
            event.getPlayer().sendMessage(message2);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!Objects.equals(player_map.get(player.getName()), player.getAddress().getHostName())) {
            player_map.remove(player.getName());
            player.sendMessage("ª∂”≠ªÿ¿¥");
        }


    }

}