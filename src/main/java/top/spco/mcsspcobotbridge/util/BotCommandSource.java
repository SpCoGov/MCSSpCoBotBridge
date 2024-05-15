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
package top.spco.mcsspcobotbridge.util;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;
import top.spco.mcsspcobotbridge.bridge.ClientHandler;
import top.spco.mcsspcobotbridge.bridge.Payload;

import java.util.UUID;

/**
 * 用于接收命令的返回值
 *
 * @author SpCo
 * @version 0.1.4
 * @since 0.1.3
 */
public class BotCommandSource implements CommandSource {
    private final Payload request;
    private final ClientHandler client;

    public BotCommandSource(ClientHandler client, Payload request) {
        this.client = client;
        this.request = request;
    }

    @Override
    public void sendMessage(Component component, UUID uuid) {
        JsonObject data = new JsonObject();
        data.addProperty("result", component.getString());
        Payload replyPayload = Payload.reply(request, data);
        client.send(replyPayload);
    }

    @Override
    public boolean acceptsSuccess() {
        return false;
    }

    @Override
    public boolean acceptsFailure() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }
}