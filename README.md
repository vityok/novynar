# Novinar #

A very simple RSS news reader and agregator. Designed to be
self-contained and without external dependencies. Relies solely on the
standard libraries provided by the Java runtime environment.

Despite being in the very early stages of development it is already
somewhat usable (at least it works for me).

### Components ###

* User interface is built on top of JavaFX: HTML content is rendered using standard WebView component
* Data storage backend is embedded Apache Derby database, which comes along with JavaSE 8
* Feeds with the feeds tree and their configuration is stored in an OPML file

### Contacts ###

Check the [project page](https://bitbucket.org/vityok/novinar/overview) for ways to leave feedback/open tickets for bug reports.

# TODO #

[Check open tickets](https://bitbucket.org/vityok/novinar/issues?status=new&status=open), there are some.

# Installation

Currently from sources only: after producing the jar file with the Ant
script (or Gradle if you like), launch the app with the `run.sh` shell
script.