# ExpBottle

Маленький плагин для Paper/Spigot 1.19.4: кастомный «Бутылёк опыта», который при использовании (ПКМ) выдаёт ровно N уровней опыта — без рандома.

## Как работает
- Количество уровней хранится в самом предмете (PersistentDataContainer).
- ПКМ бутыльком → съедается 1 штука, игрок получает +N уровней, играет звук и частицы.
- Обычные ванильные бутыльки не трогаются (работают как обычно).

## Команда
```
/expbottle give <игрок> <уровни> [кол-во]
```
Право: `expbottle.give` (по умолчанию — только OP/консоль).

Пример для DeluxeMenus:
```
[console] expbottle give %player_name% 15 1
```

## Сборка через GitHub Actions
1. Залей содержимое этой папки в репозиторий GitHub (сохрани структуру папок).
2. Открой вкладку **Actions** → workflow «Build ExpBottle» запустится автоматически при push (или Run workflow вручную).
3. Скачай артефакт **ExpBottle-jar** → внутри `ExpBottle-1.0.0.jar`.
4. Кинь jar в `plugins/`, перезапусти сервер.

## Структура
```
pom.xml
.github/workflows/build.yml
src/main/resources/plugin.yml
src/main/java/top/lordgamer/expbottle/ExpBottlePlugin.java
```
