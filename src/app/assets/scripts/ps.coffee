window.urls =
  entries: "/json/entries"


class Entry extends Backbone.Model

class EntryCollection extends Backbone.Collection
  model: Entry
  url: window.urls.entries

class EntryListView extends Backbone.View
  template: _.template($("#entryListTemplate").html())
  entries: null

  render: () ->
    $(".globalContainer").html(
      @template(
        entries: @entries
      )
    )


class Application extends Backbone.Router
  views:
    entryList: new EntryListView()

  routes:
    "" : "index"

  index: () ->
    entries = new EntryCollection()
    entries.fetch().done($.proxy(
      () ->
        @views.entryList.entries = entries
        @views.entryList.render()
      @
      )
    )

$ ->
  window.app = new Application()
  Backbone.history.start()