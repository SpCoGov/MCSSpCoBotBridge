/*
 * Copyright 2023 SpCo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.spco.mcsspcobotbridge.bridge;

import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import top.spco.mcsspcobotbridge.Config;
import top.spco.mcsspcobotbridge.util.BotCommandSourceStack;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created on 2024/03/09 16:06
 *
 * @author SpCo
 * @version 0.1.4
 * @since 0.1.0
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String name = "undefined";
    private final HeartbeatTimer heartbeatTimer = new HeartbeatTimer(this);

    @Override
    public void run() {
        try {
            heartbeatTimer.start();
            send(Payload.hello());
            String inputLine;
            try {
                while ((inputLine = in.readLine()) != null) {
                    if (Config.debug) {
                        LOGGER.info("Received from {}: {}", getAddress(), inputLine);
                    }
                    Payload payload = Payload.fromJson(inputLine);
                    if (name.equals("undefined")) {
                        if (payload.getOperationCode() == 3) {
                            if (payload.getData().getAsString().equals("undefined")) {
                                send(Payload.invalidSession("'undefined' cannot be used as a name"));
                                return;
                            }
                            this.name = payload.getData().getAsString();
                            BridgeServer.getInstance().getClientManager().register(this, name);
                            continue;
                        }
                        return;
                    }
                    switch (payload.getOperationCode()) {
                        case 1 -> {
                            this.heartbeatTimer.reset();
                            send(Payload.heartbeatAck(payload, new JsonObject()));
                        }
                        case 5 -> {
                            if (payload.getData() instanceof JsonObject data) {
                                switch (data.get("type").getAsString()) {
                                    case "CALL_COMMAND" -> {
                                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                        if (server == null) {
                                            JsonObject responseData = new JsonObject();
                                            responseData.addProperty("result", "服务器处于启动阶段，无法执行命令");
                                            Payload replyPayload = Payload.reply(payload, responseData);
                                            send(replyPayload);
                                        } else {
                                            CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
                                            if (data.has("command")) {
                                                var bcs = new BotCommandSourceStack(server,this, payload);
                                                try {
                                                    dispatcher.execute(data.get("command").getAsString(), bcs);
                                                } catch (CommandSyntaxException e) {
                                                    bcs.sendFailure(new TextComponent(e.getMessage()));
                                                }
                                            }
                                        }

                                    }
                                    case "GROUP_MESSAGE" -> {
                                        String name = data.get("sender_name").getAsString();
                                        String message = data.get("message").getAsString();
                                        MutableComponent component = new TextComponent(name).setStyle(Style.EMPTY.withColor(TextColor.parseColor("#545454")));
                                        component.append(new TextComponent(" → ").setStyle(Style.EMPTY.withColor(TextColor.parseColor("#5454fb")).withBold(true)))
                                                .append(new TextComponent(message).setStyle(Style.EMPTY.withColor(TextColor.parseColor("#a8a8a7"))));

                                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                                        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                                            player.sendMessage(component, ChatType.SYSTEM, Util.NIL_UUID);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (Config.debug) {
                    LOGGER.info("Disconnected from {}: Client actively closes", name);
                }
            } catch (Exception e) {
                if (BridgeServer.getInstance().getClientManager().isRegistered(name)) {
                    if (Config.debug) {
                    LOGGER.info("Disconnected from {}: Exception thrown", name);
                }
                    LOGGER.info("{}({}) disconnected: {}", name, this.getAddress(), e.getMessage());
                    BridgeServer.getInstance().getClientManager().remove(this.name);
                }
            }
        } finally {

            close();
        }
    }

    /**
     * Send data to the client
     *
     * @param payload data
     */
    public void send(Payload payload) {
        out.print(payload.toString());
        out.flush();
        if (Config.debug) {
            LOGGER.info("Sent to {}: {}", getAddress(), payload);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            InputStreamReader is = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8);
            this.in = new BufferedReader(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.in.close();
            this.out.close();
            this.clientSocket.close();
            this.heartbeatTimer.timer.cancel();
            if (BridgeServer.getInstance().getClientManager().isRegistered(name)) {
                BridgeServer.getInstance().getClientManager().remove(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getAddress() {
        return this.clientSocket.getInetAddress() + ":" + this.clientSocket.getPort();
    }

    static class HeartbeatTimer {
        private Timer timer;
        private final ClientHandler handler;

        public HeartbeatTimer(ClientHandler handler) {
            this.handler = handler;
            this.timer = new Timer();
        }

        public void start() {
            timer.schedule(new HeartbeatTask(handler), Config.heartbeatInterval + 1000, Config.heartbeatInterval + 1000);
        }

        public void reset() {
            timer.cancel();
            timer = new Timer();
            start();
        }

        private static class HeartbeatTask extends TimerTask {
            private final ClientHandler handler;

            public HeartbeatTask(ClientHandler handler) {
                this.handler = handler;
            }

            @Override
            public void run() {
                if (Config.debug) {
                    LOGGER.info("Disconnected from {}: Heartbeat timeout", handler.name);
                }
                this.handler.close();
            }
        }
    }
}