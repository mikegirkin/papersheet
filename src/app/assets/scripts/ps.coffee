window.urls =
  entries: "/json/entries"
  entryGroups: "/json/entrygroups"

class Layout
  mainArea: $("#mainArea")
  leftSidebar: $("#leftSidebar")

class Entry extends Backbone.Model

class EntryCollection extends Backbone.Collection
  model: Entry
  url: window.urls.entries

class EntryGroup extends Backbone.Model

class EntryGroupCollection extends Backbone.Collection
  model: EntryGroup
  url: window.urls.entryGroups

class EntryListView extends Backbone.View
  template: _.template($("#entryListTemplate").html())
  entries: null

  render: () ->
    layout.mainArea.html(
      @template(
        entries: @entries
      )
    )

class EntryGroupListView extends Backbone.View
  template: _.template($("#entryGroupListTemplate").html())
  entryGroups: null

  render: () ->
    layout.leftSidebar.html(
      @template(
        entryGroups: @entryGroups
      )
    )

class Application extends Backbone.Router
  views:
    entryList: new EntryListView()
    entryGroupList: new EntryGroupListView()

  routes:
    "" : "index"

  index: () ->
    @entries = new EntryCollection()
    @groups = new EntryGroupCollection()
    $.when(
      @entries.fetch(),
      @groups.fetch()
    ).done($.proxy(
      () ->
        @views.entryList.entries = @entries
        @views.entryList.render()

        @views.entryGroupList.entryGroups = @groups
        @views.entryGroupList.render()
      @
      )
    )

$ ->
  window.app = new Application()
  window.layout = new Layout()
  Backbone.history.start()