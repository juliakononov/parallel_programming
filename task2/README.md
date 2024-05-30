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
* `ThreadSanitizer: unlock of an unlocked mutex`
* `ThreadSanitizer: data race`
* `ThreadSanitizer: lock-order-inversion`

Всего 14 `WARNING`

### Helgrind
Программа запускалась таким образом:
```
valgrind --tool=helgrind ./crazy 4
```

Запуск вывел `800 errors from 31 contexts`. Все ошибки были связаны с гонкой данных.

### Причины
После более детального анализа кода стало понятно, что ситуации возникновения ошибок невозможны из-за особенности задачи:
* Профессор был один => можно было лишь единожды (сразу после старта) запустить функцию `Professor`, которая выполняется в одном потоке. Поэтому доступ к мьютексам внутри этой функции всегда был эксклюзивен (мьютекс  использовался в качестве блокировки в контексте conditional variable(`pthread_cond_wait()`)).

### Гонка данных
Добавлена искусственная гонка данных, чтобы проверить работу инструментов. 

Был убран мьютекс, который отвечал за корректное взаимодействие с глобальными переменными.

После запуска преобразованного кода, используя `Thread sanitizer` , программа вывела следующее предупреждение, указывающее на измененную часть кода:
```
WARNING: ThreadSanitizer: data race (pid=21815)
```

`Helgrin` тоже с задачей справился и вывел соответсующие предупреждения

<details><summary>Пример предупреждения</summary>

```
==27303== Possible data race during write of size 4 at 0x10C0CC by thread #4
==27303== Locks held: 1, at address 0x10C220
==27303==    at 0x109825: startStudent (in /home/julia/Crazy-Professor-Synchronization/CPS/crazy)
==27303==    by 0x491E1CE: ??? (in /usr/lib/libc.so.6)
==27303==    by 0x499F503: clone (in /usr/lib/libc.so.6)
==27303== 
==27303== This conflicts with a previous read of size 4 by thread #2
==27303== Locks held: none
==27303==    at 0x10967A: startProfessor (in /home/julia/Crazy-Professor-Synchronization/CPS/crazy)
==27303==    by 0x491E1CE: ??? (in /usr/lib/libc.so.6)
==27303==    by 0x499F503: clone (in /usr/lib/libc.so.6)
==27303==  Address 0x10c0cc is 0 bytes inside data symbol "numStud"
```

</details>
