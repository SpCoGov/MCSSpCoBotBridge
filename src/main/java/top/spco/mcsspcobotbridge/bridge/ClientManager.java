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

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to manage all clients of Bridge Server
 *
 * @author SpCo
 * @version 0.1.3
 * @since 0.1.0
 */
public class ClientManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, ClientHandler> clientMap = new HashMap<>();

    public void register(ClientHandler client, String name) {
        int i = 0;
        String rootName = name;
        // 同名字未被注冊
        if (!clientMap.containsKey(name)) {
            register(name, client);
        } else {

            // 给待注册的名字添加序号
            do {
                name = rootName + "-" + ++i;
            } while (clientMap.containsKey(name));
            register(name, client);

        }
    }

    public Set<String> getAllClient() {
        return clientMap.keySet();
    }

    public void pushToAll(Payload data) {
        for (String name : getAllClient()) {
            clientMap.get(name).send(data);
        }
    }

    private void register(String name, ClientHandler client) {
        clientMap.put(name, client);
        client.setName(name);
        LOGGER.info("{}({}) registered", name, client.getAddress());
    }

    public void remove(String name) {
        clientMap.remove(name);
    }

    public boolean isRegistered(String name) {
        return clientMap.containsKey(name);
    }
}