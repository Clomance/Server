# Server
Сервер для курсовой работы

## Начало работы

Вам понадобится JDK 8 либо любая другая подходящая версия.

Также нужны разрешения на использование интернета программой и на создание файлов.

Запустить сеё чудо можно через любую удобную для вас среду программирования или [с помощью командной строки](https://habr.com/ru/post/125210/)

## Информация о программе
1. Не принимает никаких внешних аргументов (аргументов среды)
2. Поддерживается консолью и управляется командами: помощь, инфо, установить, показать, запуск и остановка (о них подробнее ниже в "Консольные команды сервера").
3. Способ работы с клиентом: одно подключение - одно действие; каждое подключение выполняет только одну задачу от подключённого клиента.
4. Одноразовая авторизация при отправке данных для расчёта происходит при помощи токенов.
5. Всего три обрабатываемые задачи: вход, регистрация и расчёт (0, 1, 2 соответственно).
6. При завершении работы все данные пользователей и настройки сохраняются в файлы, которые находятся в той же папке, что и программа.

## Принцип работы
1. После запуска идёт проверка файлов с данными пользователей и настройками сервера, после загрузка их содержимого, если они не пустные. При отстутствии файлов они создаются в локальной папке программы.
2. Далее открывается консоль и сервер переходит в состояние ожидания команд.
3. Команда `запуск` запускает допольнительный (серверный) поток, который регистрирует сервер, создаёт 
среду для обработки (дополнительные потоки, флаги и т.д.) и обрабатывает приходящие подключения (клиенты). 
При получении клиента выбирается свободный поток из среды и управление подключением переходит ему. 
Клиент отправляет номер задачи (0, 1 или 2) и нужные данные, после ему отправляется ответ и соединение сбрасывается, 
и поток переходит в режим ожидания нового подключения.
4. Команда `остановка` останавливает работу сервера: закрывает серверный поток и все его дочернии, а так же сбрасывает все подключения.
После сохраняет все данные пользователей и настройки сервера в файлы.

## Консольные команды сервера
1. помощь - выводит список всех команд
2. запуск - запускает сервер (запустить сервер дважды не получится)
3. установить [поле] [значение]- устанавливает значение полю.
```
[поле] может принимать следующие значения:
> адрес - адрес сервера
> порт - порт сервера
> колВоПодключений - максимальное количество одновременных подключений
```
4. инфо - выводит основную информацию о сервере (адрес, порт и максимальное количество одновременных подключений).
5. показать [arg] - выводит определённую информацию о сервера и его данных. Используется для отладки.
```
Arg может принимать следующие значения:
> главное - все логины и пароли
> токены - все токены профилей
> авторы - авторы сервера
```
5. остановка - останавливает работу сервера.
