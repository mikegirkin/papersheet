window.urls =
  entries: "/json/entries"
  entryGroups: "/json/entrygroups"

class Layout
  mainArea: $("#mainArea")
  leftSidebar: $("#leftSidebar")

class Entry extends Backbone.Model
  urlRoot: window.urls.entries

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

  initialize: (entries) ->
    @entries = entries
    @entries.on("reset", @onEntriesReset, @)

  render: () ->
    layout.mainArea.html(
      @template(
        entries: @entries
      )
    )
    @$el = $("#entryListViewContainer")
    @$el.find('#createNewEntryBtn').click($.proxy(@onCreateRequested, @))
    @$el.find(".changeEntryState").on("click", $.proxy(@onChangeEntryStateRequested, @))
    @$el.find(".editEntry").click($.proxy(@onEditEntryRequested, @))

  onCreateRequested: (e) ->
    e.preventDefault()
    content = @$el.find("#newEntryContent").val()
    entry = new Entry(
      content: content
      stateId: 1
      groupId: 1
    )
    entry.save().done($.proxy(
      () -> @entries.fetch({reset: true})
      @
    ))

  onEntriesReset: () ->
    @render()

  onChangeEntryStateRequested: (e) ->
    e.preventDefault()
    entryId = $(e.target).closest(".entryRow").attr('data-id')
    console.log('entryId:' + entryId)

  onEditEntryRequested: (e) ->
    e.preventDefault()



class EntryGroupListView extends Backbone.View
  template: _.template($("#entryGroupListTemplate").html())
  entryGroups: null
  $el: null

  render: () ->
    layout.leftSidebar.html(
      @template(
        entryGroups: @entryGroups
      )
    )

class Application extends Backbone.Router
  views:
    entryList: null
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
        @views.entryList = new EntryListView(@entries)
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