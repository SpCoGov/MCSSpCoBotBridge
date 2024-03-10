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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import top.spco.mcsspcobotbridge.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server
 *
 * @author SpCo
 * @version 0.1.0
 * @since 0.1.0
 */
public class BridgeServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static BridgeServer instance;
    private final ClientManager clientManager = new ClientManager();
    private final int port = Config.port;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10, new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Server ClientHandler-" + threadNumber.getAndIncrement());
            return thread;
        }
    });

    private BridgeServer() {
        Thread thread = new Thread(() -> {
            try (ServerSocket socket = new ServerSocket(port)) {
                LOGGER.info("MSSBB server has been started, port: {}", port);
                while (true) {
                    Socket clientSocket = socket.accept();
                    executorService.submit(new ClientHandler(clientSocket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        },"Bridge Thread");
        thread.start();
    }

    /**
     * Get server instance
     *
     * @return server instance
     */
    public static BridgeServer getInstance() {
        if (instance == null) {
            instance = new BridgeServer();
        }
        return instance;
    }

    public ClientManager getClientManager() {
        return clientManager;
    }
}