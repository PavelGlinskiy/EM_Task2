# TCP Echo Server (RFC 862)

Простой многопоточный **TCP Echo Server** и клиент на Java с использованием Docker Compose.  
Сервер принимает подключения от клиентов и возвращает им обратно отправленные сообщения (**echo**).

---

##  Запуск проекта

Перейдите в папку `e2` и выполните команду:

```bash
docker compose up --build
```

После сборки и запуска:
- **Сервер** (`echo-server`) слушает порт `8007`.
- **Клиенты** (`echo-client-1`, `echo-client-2`, `echo-client-3`) автоматически подключаются, отправляют сообщения и завершаются.

Пример логов:
```
echo-server    | [TcpEchoServer] Server started on port 8007
echo-client-1  | Connected to echo-server:8007
echo-client-1  | Received: Hello from client 1!
echo-client-2  | Connected to echo-server:8007
echo-client-2  | Received: Hello from client 2!
echo-client-3  | Connected to echo-server:8007
echo-client-3  | Received: Hello from client 3!
```

---

##  Запуск клиента вручную

Можно запустить дополнительного клиента, не редактируя `docker-compose.yml`:

```bash
docker compose run --rm echo-server java -cp out org.example.TcpEchoClient echo-server 8007 "Hello from aut client!"
```

---

##  Структура проекта

- `TcpEchoServer.java` — многопоточный TCP Echo сервер.  
- `TcpEchoClient.java` — TCP клиент.  
- `Dockerfile` — инструкция для сборки контейнера с Java-приложением.  
- `docker-compose.yml` — поднимает сервер и тестовых клиентов.  
- `README.md` — документация по запуску.
