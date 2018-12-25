# Novinar #

Novinar (ukr. *Новинар*) -- a very simple RSS news reader and
agregator. Designed to be self-contained and without external
dependencies. Relied solely on the standard libraries provided by the
JavaSE 8 runtime environment.

However, Java 11 has introduced a very significant backwards
incompatible change: JavaFX is no longer a part of the standard
distribution and has to be manually installed from a third-party
[Web-site](https://openjfx.io/).

This adds a second external dependency on the project: one is Apache
Derby database, and the second is the JavaFX UI toolkit.

Despite being in the very early stages of development it is already
usable (at least it works for me).

### Components ###

* User interface is built on top of JavaFX: HTML content is rendered using standard WebView component
* Data storage backend is embedded Apache Derby database, which come along with JavaSE 8
* Feeds with the feeds tree and their configuration is stored in an OPML file

**Designed for JavaSE 11**, the current (as of 2018) long-time support
(LTS) Java platform.

### Contacts ###

Check the [project page](https://bitbucket.org/vityok/novinar/overview) for ways to leave feedback/open tickets for bug reports.

# TODO #

[Check open tickets](https://bitbucket.org/vityok/novinar/issues?status=new&status=open), there are some.

# Installation

Currently from sources only: after producing the jar file with the Ant
script (or Gradle if you like), launch the app with the `run.sh` shell
script.

Make sure that the `PATH_TO_FX` environment variable points to the
unpacked OpenJFX (JavaFX) SDK directory.