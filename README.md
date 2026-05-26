# Auto Vault

[English](#english) | [Русский](#русский)

> **This branch (`mc-26.1`)** targets **Minecraft 26.1.2** with **Fabric Loader 0.19.2** on **Java 25** (uses official Mojang mappings).
> The `mc-1.21.11` branch contains the same mod for Minecraft 1.21.11 / Java 21.

---

## English

A Minecraft Fabric client-side mod that automatically opens Vaults in Trial Chambers and filters dropped items (Mace, Heavy Core, Trident, Enchanted Books, etc.).

### Features

- **Cursor Targeting:** Detects the exact Vault block you are looking at under your crosshair (using default client block reach).
- **Smart Keys matching:** Knows when to use a standard `Trial Key` or `Ominous Trial Key` on the respective Vault types.
- **Visual Display:** Outputs the name of the currently spinning item in the focused Vault directly above your hotbar (action bar).
- **Item Filtering:** Reads the internal item currently displayed by the Vault and only opens the Vault if the item matches your whitelist.
- **Wind Burst Verification:** For Enchanted Books, can specifically check if the book has the `Wind Burst` enchantment before opening.
- **Interactive GUI:** Easy configuration of target Vault types, item whitelist presets, and custom item ID support.

### Controls

- **`G`:** Toggle the Auto Vault mod on/off.
- **`K`:** Open the Configuration GUI screen.

### Configuration

The mod configuration is saved to `.minecraft/config/autovault.json` and can be adjusted through the GUI:
- **Open Ominous Vaults:** Toggle opening ominous trial vaults.
- **Open Normal Vaults:** Toggle opening normal trial vaults.
- **Item Filter:** Toggle the whitelist check.
- **Item Presets:** Quick checkmarks for `Trident`, `Mace`, `Heavy Core`, and `Enchanted Book`.
- **Add / Remove:** Field to register any custom Minecraft item identifier (e.g. `minecraft:diamond`).

---

## Русский

Клиентский Fabric-мод для Minecraft, который автоматически открывает Хранилища (Vaults) в Испытательных комнатах (Trial Chambers) и фильтрует выпадающие предметы (Булава, Тяжёлое ядро, Трезубец, Зачарованные книги и т.д.).

### Возможности

- **Наведение по курсору:** Мод определяет конкретный сейф, на который наведён ваш прицел (используя стандартную дальность клика игрока).
- **Умное соответствие ключей:** Автоматически подбирает обычный `Ключ испытаний` или `Зловещий ключ испытаний` для соответствующих типов сейфов.
- **Отображение предметов:** Выводит название крутящегося предмета над вашим хотбаром (action bar) в реальном времени.
- **Фильтрация предметов:** Считывает отображаемый предмет внутри Хранилища и производит активацию только в том случае, если этот предмет находится в белом списке.
- **Проверка на Wind Burst (Ветровой шквал):** Для Зачарованных книг можно включить проверку наличия именно чар `Wind Burst` перед тратой ключа.
- **Удобный интерфейс:** Полная настройка типов хранилищ, пресетов предметов и ручного добавления ID через игровое меню.

### Управление

- **`G`:** Быстрое включение / выключение работы мода.
- **`K`:** Открытие графического интерфейса конфигурации.

### Настройка

Файл конфигурации сохраняется в `.minecraft/config/autovault.json` и настраивается через игровое меню:
- **Open Ominous Vaults:** Включение/выключение открытия зловещих хранилищ.
- **Open Normal Vaults:** Включение/выключение открытия обычных хранилищ.
- **Item Filter:** Включение/выключение фильтрации предметов.
- **Item Presets:** Чекбоксы для быстрого выбора популярных предметов (`minecraft:trident`, `minecraft:mace`, `minecraft:heavy_core`, `minecraft:enchanted_book`).
- **Add / Remove:** Поле ввода для добавления любого другого идентификатора предмета (например, `minecraft:diamond`).
