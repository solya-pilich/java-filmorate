# java-filmorate
## Схема базы данных:
![Схема базы данных](diagram.png)
### Описание схемы:
- **films** - хранит информацию о фильмах
- **users** - хранит информацию о пользователях
- **genre** - справочник жанров
- **film_genre** - связь фильмов с жанрами
- **film_likes** - связь пользователей с фильмами, которые им понравились
- **friendship** - отношения дружбы между пользователями. Статус ограничен двумя значениями (PENDING / CONFIRMED) через CHECK-ограничение
- **mpa_rating** - справочник рейтингов MPA с допустимыми значениями: G, PG, PG-13, R, NC-17.

### Примеры запросов
1. **Получение всех фильмов**
```sql
SELECT * FROM films;
```
2. **Получение всех пользователей**
```sql
SELECT * FROM users;
```
3. **Получение пользователя по id**
```sql
SELECT * FROM users WHERE id = ?;
   ```
4. **Добавление лайка**
```sql
INSERT INTO film_likes (film_id, user_id) VALUES (?, ?);
```
5. **Удаление лайка**
```sql
DELETE FROM film_likes WHERE film_id = ? AND user_id = ?;
```
6. **Топ-10 фильмов по лайкам**
```sql
SELECT f.*, COUNT(fl.user_id) AS likes_count
FROM films AS f
LEFT JOIN film_likes AS fl ON f.id = fl.film_id
GROUP BY f.id, f.name, f.description, f.releaseDate, f.duration, f.rating_id
ORDER BY likes_count DESC
LIMIT 10;
```
7. **Получение фильма с его жанрами и MPA-рейтингом**
```sql
SELECT f.id, f.name, f.description, f.releaseDate, f.duration,
       mr.name AS mpa_rating,
       g.name AS genre_name
FROM films AS f
LEFT JOIN mpa_rating AS mr ON f.rating_id = mr.id
LEFT JOIN film_genre AS fg ON f.id = fg.film_id
LEFT JOIN genre AS g ON fg.genre_id = g.id
WHERE f.id = ?;
```
8. **Общие друзья пользователей 1 и 2**
```sql
SELECT f1.friend_id
FROM friendship AS f1
JOIN friendship AS f2 ON f1.friend_id = f2.friend_id
WHERE f1.user_id = ?
AND f2.user_id = ?;
```
9. **Получение списка друзей пользователя с их статусами**
```sql
SELECT u.*, f.friendshipStatus
FROM friendship AS f 
JOIN users AS u ON u.id = f.friend_id
WHERE f.user_id = ?
```

