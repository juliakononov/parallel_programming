# Инструменты анализа кода

## Анализируемый проект
[Выбранный репозиторий](https://github.com/renanGit/Crazy-Professor-Synchronization/tree/master) рассматривает задачу о сумасшедшем профессоре и учениках, которые хотят задать вопросы.
Для этого студенты должны синхронизироваться друг с другом и с профессором. Подробнее с задачей можно ознакомиться 
[здесь](https://github.com/renanGit/Crazy-Professor-Synchronization/blob/master/README.md).

Проект написан на `C` без элементов `OpenMP`.

## Анализ
### Thread sanitizer
Компиляция файла происходила с помощью флага `-fsanitize=thread`.

Запуск `./carzy 4` выводит следующие предупреждения:
* `ThreadSanitizer: data race`
* `ThreadSanitizer: lock-order-inversion`

Всего 14 `WARNING`

### Helgrind
Программа запускалась таким образом:
```
valgrind --tool=helgrind ./crazy 4
```

Все выведенные ошибки были связаны с гонкой данных.

### Причины
После более детального анализа кода стало понятно, что ситуации возникновения ошибок невозможны из-за особенности задачи:
* Профессор был один => можно было лишь единожды (сразу после старта) запустить функцию `Professor`, которая выполняется в одном потоке. Поэтому доступ к мьютексам внутри этой функции всегда был эксклюзивен 
  * Мьютекс  использовался в качестве блокировки в контексте conditional variable(`pthread_cond_wait()`)
  * Несмотря на то, что локи являются глобальными, используются они только внутри функции `Professor`

### Гонка данных
Добавлена искусственная гонка данных, чтобы проверить работу инструментов. 

Был убран мьютекс, который отвечал за корректное взаимодействие с глобальными переменными (в часности `numStud`).

После запуска преобразованного кода, используя `Thread sanitizer` , программа вывела следующее предупреждение, указывающее на измененную часть кода:
```
WARNING: ThreadSanitizer: data race (pid=21815)
  Write of size 4 at 0x5648b724710c by thread T2:
    #0 startStudent ./crazyprofessor.c:112 (crazy+0x19e1) (BuildId: fe80fc5c0b9efbe8374b9c1edb533629321025b8)

  Previous read of size 4 at 0x5648b724710c by thread T1:
    #0 startProfessor ./crazyprofessor.c:67 (crazy+0x1760) (BuildId: fe80fc5c0b9efbe8374b9c1edb533629321025b8)

  Location is global 'numStud' of size 4 at 0x5648b724710c (crazy+0x510c)
  
  <...>

SUMMARY: ThreadSanitizer: data race ./crazyprofessor.c:112 in startStudent
```

`Helgrin` тоже с задачей справился и вывел соответсующие предупреждения:

```
<...>

==27303== Possible data race during write of size 4 at 0x10C0CC by thread #4
==27303== Locks held: 1, at address 0x10C220
==27303==    at 0x109825: startStudent (in ...)
==27303==    by 0x491E1CE: ??? (in /usr/lib/libc.so.6)
==27303==    by 0x499F503: clone (in /usr/lib/libc.so.6)
==27303== 
==27303== This conflicts with a previous read of size 4 by thread #2
==27303== Locks held: none
==27303==    at 0x10967A: startProfessor (in ...)
==27303==    by 0x491E1CE: ??? (in /usr/lib/libc.so.6)
==27303==    by 0x499F503: clone (in /usr/lib/libc.so.6)
==27303==  Address 0x10c0cc is 0 bytes inside data symbol "numStud"

<...>
```

## Итоги
`Thread sanitizer`, `Helgrin` подходят для анализа проектов, содержащих элементы параллельного программирования. Однако существуют ложные срабатывания. Верить каждой выыводимой ошибке не стоит. 
