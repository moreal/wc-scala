[wc] written in Scala
=====================

It is a project to practice Scala by implementing [wc] (**w**ord **c**ount) program.


[wc]: https://en.wikipedia.org/wiki/Wc_(Unix)


Prerequisite
------------

```
brew install scala
```


Test
----

You can run both `wc-scala` and `wc` at once by the following command:

```
cat [path to file] | ./scripts/scala_and_wc.sh [...options]
# Example:
# cat fixtures/foo | ./scripts/scala_and_wc.sh -w -c -l
```
