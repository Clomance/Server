# Server
Сервер для курсовой работы

## Начало работы

Вам понадобится JDK 8 либо любая другая подходящая версия.

Также нужны разрешения на использование интернета программой.

Запустить сеё чудо можно через любую удобную для вас среду программирования или [с помощью командной строки](https://habr.com/ru/post/125210/)

## Информация о программе
1. Не принимает никаких внешних аргументов (аргументов среды)
2. Поддерживается консолью и управляется командами: help, info, set, show, start и stop (о них подробнее ниже в "Консольные команды сервера").
3. Способ работы с клиентом: одно подключение - одно действие; каждое подключение выполняет только одну задачу от подключённого клиента.
4. Одноразовая авторизация при отправке данных для расчёта происходит при помощи токенов.
5. Всего три обрабатываемые задачи: вход, регистрация и расчёт (0, 1, 2 соответственно).

## Принцип работы
1. После запуска идёт проверка файлов с данными пользователей и загрузка последних, если имеются, либо создание файлов, если те отсутстуют.
2. Далее открывается консоль и сервер переходит в состояние ожидания команд.
3. Команда start запускает допольнительный (серверный) поток, который регистрирует сервер, создаёт 
среду для обработки (дополнительные потоки, флаги и т.д.) и обрабатывает приходящие подключения (клиенты). 
При получении клиента выбирается свободный поток из среды и управление подключением переходит ему. 
Клиент отправляет номер задачи (0, 1 или 2) и нужные данные, после ему отправляется ответ и соединение сбрасывается, 
и поток переходит в режим ожидания нового подключения.
4. Команда stop останавливает работу сервера: закрывает серверный поток и все его дочернии, а так же сбрасывает все подключения.
После сохраняет все данные в файлы.

## Консольные команды сервера
1. help - выводит список всех команд
2. start - запускает сервер (запустить сервер дважды не получится)
3. set [arg] - устанавливает значение, которое вписывается позже, полю [arg].
```
Arg может принимать следующие значения:
> ip - адрес сервера
> port - порт сервера
> conAmount - максимальное количество одновременных подключений
```
4. info - выводит основную информацию о сервере (адрес, порт и максимальное количество одновременных подключений).
5. show [arg] - выводит определённую информацию о сервера и его данных. Используется для отладки.
```
Arg может принимать следующие значения:
> mains - все логины и пароли
> tokens - все токены профилей
```
5. stop - останавливает работу сервера.
