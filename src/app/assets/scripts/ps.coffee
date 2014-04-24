window.urls =
  entries: "/json/entries"

class Application extends Backbone.Router
  routes:
    "" : "index"

  index: () ->
    console.log("Index view")

class Entry extends Backbone.Model
  url: window.urls.entries

window.app = new Application()
Backbone.history.start()