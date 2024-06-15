// -*- coding: UTF-8 -*-
package org.mcu.papermc;


import com.google.gson.Gson;
import org.bukkit.Bukkit;
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

public class McuLoginPlugin extends JavaPlugin implements Listener {
    public static class Response_Message {
        int code;
        String message;

        public Response_Message(String message) {
            this.code = 400;
            this.message = message;
        }
    }

    HashMap<String, String> player_map = new HashMap<>();
    String uri = "http://10980xe.mc5173.cn:10124/api";

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("bind").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

                if (args.length == 0) {
                    player.sendMessage("∞Û∂®’À∫≈/bind <password> <token>");
                    return true;
                }

                var token = args[1];
                var password = args[0];

                Response_Message response = doPost_bind_player(uri, token, player.getName(), password);
                if (response.code == 200) {
                    player_map.put(player.getName(), player.getAddress().getHostName());
                }
                player.sendMessage(response.message);
            } else {
                sender.sendMessage("ƒ„≤ª «ÕÊº“!");
            }
            return true;
        });

        getCommand("login").setExecutor((sender, command, label, args) -> {
            if (sender instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;

                if (args.length == 0) {
                    String message = "∞Û∂®µ«¬º/login <password>";
                    player.sendMessage(message);
                    return true;
                }
                var password = args[0];
                Response_Message response = doGet_auth(uri, password);
                if (response.code == 200) {
                    player_map.put(player.getName(), player.getAddress().getHostName());
                }
                player.sendMessage(response.message);
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
            String message2 = "»ÙŒﬁ’Àªß£¨«Î∞Û∂®’À∫≈/bind <password> <token>";
            event.getPlayer().sendMessage(message2);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (!Objects.equals(player_map.get(player.getName()), player.getAddress().getHostName())) {
            player_map.remove(player.getName());
        }


    }

}