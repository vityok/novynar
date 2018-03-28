# Novinar #

A very simple RSS news reader and agregator. Designed to be
self-contained and without external dependencies. Relies solely on the
standard libraries provided by the Java runtime environment.

Not yet usable, in the very early stages of development.

### Components ###

* User interface is built on top of JavaFX: HTML content is rendered using standard WebView component
* Data storage backend is embedded Apache Derby database, which comes along with JavaSE 8
* Feeds with the feeds tree and their configuration is stored in an OPML file

### Contacts ###

Check the [project page](https://bitbucket.org/vityok/novinar/overview) for ways to leave feedback/open tickets for bug reports.

# TODO #

* don't store channel info in the db, rely on the opml file for
  that. OPMLManager should be the one responsible for managing the
  channel info. DB will rely on the channel IDs provided by the
  OPMLManager