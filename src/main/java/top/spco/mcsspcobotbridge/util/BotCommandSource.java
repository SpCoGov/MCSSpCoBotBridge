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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;
import top.spco.mcsspcobotbridge.bridge.ClientHandler;
import top.spco.mcsspcobotbridge.bridge.Payload;

import java.util.function.Supplier;

/**
 * Created on 2024/03/09 19:59
 *
 * @author SpCo
 * @version 0.1.0
 * @since 0.1.0
 */
public class BotCommandSource extends CommandSourceStack {
    private static final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    private final ClientHandler client;
    private final Payload request;

    public BotCommandSource(ClientHandler client, Payload request) {
        super(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO,
                server.getLevel(Level.OVERWORLD), 4,
                "SpCoBot", Component.literal("SpCoBot"), server,
                null);
        this.client = client;
        this.request = request;
    }

    private void reply(Component component) {
        JsonObject data = new JsonObject();
        data.addProperty("result", component.getString());
        Payload replyPayload = Payload.reply(request, data);
        client.send(replyPayload);
    }

    @Override
    public void sendSuccess(Supplier<Component> result, boolean p_289007_) {
        reply(result.get());
    }

    @Override
    public void sendFailure(Component p_81353_) {
        reply(p_81353_);
    }
}