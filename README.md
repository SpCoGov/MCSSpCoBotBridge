MSSBB(**M**inecraft**S**erver-**S**pCo**B**ot**B**ridge Minecraft服务器-SpCoBot桥接器) 是一个仅服务端需安装的mod，用于让SpCoBot与Minecraft服务器进行通信，如向服务器发送命令并获取返回值、订阅事件等。

# 原理
客户通过`Socket`与MSSBB建立长链接通信管道，当需要事件通知的时候MSSBB会通过`Socket`连接下发给客户。
客户需要维护`Socket`长链接的状态，包括连接状态维护、登录鉴权、心跳维护等。

## 数据结构Payload
`Payload`代表一个消息载体。包含操作码、数据和类型三个主要字段，用于描述和传输消息。
```
{
  "op": 0,
  "d": {},
  "t": "EVENT_TYPE"
}
```
|字段 |描述  |
|----|------|
|op  |操作码 |
|d   |数据   |
|t   |类型   |

## 操作码列表
所有操作码及其含义如下：
| Code | 名称             | 客户端操作      | 描述                                        |
|------|------------------|----------------|---------------------------------------------|
| 0    | Hello            | Receive        | 当客户端连接到服务器时，MSSBB下发的第一条消息  |
| 1    | Heartbeat        | Send           | 客户端发送心跳                               |
| 2    | Dispatch         | Receive        | MSSBB消息推送                               |
| 3    | Identify         | Send           | 客户端发送鉴权                               |
| 4    | Invalid Session  | Receive        | 在Identify时，如果参数错误，MSSBB将返回此消息 |
| 5    | Request          | Send           | 客户端发送请求                               |

## 默认配置
默认mod配置如下：
```
#Server name.
name = "server"
#Port of the MSSBB server.
#Range: 1024 ~ 65535
port = 58964
#Heartbeat interval (milliseconds).
#Range: 5000 ~ 10000
heartbeat_interval = 5000
debug = false
```
|字段                |描述                                                           |
|-------------------|---------------------------------------------------------------|
|port               |`Socket`MSSBB的端口，默认为`58964`                               |
|name               |Minecraft服务器的名称，用于区分，MSSBB发送Hello消息时会包含这个字段 |
|heartbeat_interval |心跳周期，单位毫秒                                               |
|debug              |debug模式开关。为`true`时会往控制台输出所有收到和发送的数据包内容   |

## 连接流程

### 连接、鉴权
与MSSBB`Socket`建立长连接后，MSSBB会发送Hello消息，如下：
```
{
  "op": 0,
  "d": {
    "heartbeat_interval": 5000,
    "name": "server_name"
  },
  "t": "HELLO"
}
```
收到Hello消息后，客户端需要向MSSBB发送Identify消息，如下：
```
{
  "op": 3,
  "d": "client_name",
  "t": "IDENTIFY"
}
```
|字段       |描述                            |
|-----------|-------------------------------|
|client_name|客户端名，按照自己的实际情况填写。|

### 心跳
鉴权完毕后需要根据MSSBB发送的Hello消息所提供的心跳周期发送心跳，如下：
```
{
  "op": 1,
  "t": "HEARTBEAT"
}
```

### 事件推送
MSSBB订阅了以下三个事件： 玩家登录、玩家登出和聊天。

玩家登录：
```
{
  "op": 2,
  "d": {
    "type": "PLAYER_LOGGED_IN",
    "player_name": "player_name"
  },
  "t": "DISPATCH"
}
```
|字段       |描述   |
|-----------|------|
|player_name|玩家名|

玩家登出：
```
{
  "op": 2,
  "d": {
    "type": "PLAYER_LOGGED_OUT",
    "player_name": "player_name"
  },
  "t": "DISPATCH"
}
```
|字段       |描述   |
|-----------|------|
|player_name|玩家名|


聊天：
```
{
  "op": 2,
  "d": {
    "type": "CHAT",
    "sender_name": "player_name",
    "message": "message"
  },
  "t": "DISPATCH"
}
```
|字段         |描述              |
|-------------|-----------------|
|sender_name |发送该消息的玩家名  |
|message     |该玩家发送的聊天消息|

### 命令发送
客户端向MSSBB发送Request消息，如下：
```
{
  "op": 5,
  "d": {
    "type": "CALL_COMMAND",
    "command": "command",
    "syn": syn
  },
  "t": "REQUEST"
}
```
|字段     |描述                                                                                               |
|--------|---------------------------------------------------------------------------------------------------|
|command |要执行的命令，不需要以`/`开头                                                                        |
|syn     |同步码，必须为整数，可以省略。如果填写了该字段，MSSBB在返回执行结果时，会在数据中附上`ack`字段且值等于同步码|

命令执行结束后如果会输出内容，服务器会发送Dispatch消息，但此时类型为REPLY，如下：
```
{
  "op": 2,
  "d": {
    "result": "result",
    "ack": ack
  },
  "t": "REPLY"
}
```
|字段   |描述                                                               |
|------|-------------------------------------------------------------------|
|result|命令执行结束后的输出                                                 |
|ack   |确认码，值等于请求的同步码，如果请求的同步码省略，则返回消息也不包含确认码|
