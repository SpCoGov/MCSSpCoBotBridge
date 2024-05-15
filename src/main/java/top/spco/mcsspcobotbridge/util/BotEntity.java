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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import top.spco.mcsspcobotbridge.bridge.ClientHandler;
import top.spco.mcsspcobotbridge.bridge.Payload;

import java.util.UUID;

/**
 * Created on 2024/03/22 15:35
 *
 * @author SpCo
 * @version 0.1.4
 * @since 0.1.4
 */
public class BotEntity extends Entity {
    private final Payload request;
    private final ClientHandler client;
    public BotEntity(Level p_19871_, Payload request, ClientHandler client) {
        super(EntityType.PLAYER, p_19871_);
        this.request = request;
        this.client = client;
        setCustomName(new TextComponent("SpCoBot"));
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    public void sendMessage(Component p_20055_, UUID p_20056_) {
        JsonObject data = new JsonObject();
        data.addProperty("result", p_20055_.getString());
        Payload replyPayload = Payload.reply(request, data);
        client.send(replyPayload);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return null;
    }
}