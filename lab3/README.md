# Лабораторная 3
## Асинхронное сетевое взаимодействие

Чтобы запустить программу, необходимо:
1. Клонировать себе репозиторий с помощью команды
```
git clone <HTTPS-ссылка на репозиторий>
```
2. Зайти в терминал
3. Перейти в директорию с лабораторной
4. Вписать в файл src/main/resources/api_keys.property свои ключи для следующих API:
```
https://docs.graphhopper.com
https://opentripmap.io
https://openweathermap.org
```
5. Выполнить команду
```
gradle run
```

Приложение выводит окно и предлагает пользователю ввести интересующее его место в поисковую строку.
После нажатия кнопки "Поиск" в окне появляется список из 10 мест, наиболее подходящих под введенную строку.
При нажатии на такое место, в окне появляется информация о погоде и 10 мест в радиусе километра с самым высоким рейтингом.
При нажатии на одно из таких мест панель с ближайшими местами заменяется на информацию о выбранном месте.
Чтобы вернуться обратно к ближайшим местам, необходимо нажать на описание.

Все вызовы API выполняются асинхронно, пользовательский интерфейс не блокируется. Приложение реализует шаблон проектирования MVC (по крайней мере старается).
