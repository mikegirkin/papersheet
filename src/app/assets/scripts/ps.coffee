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
    @entries.on("reset change add remove", @onEntriesReset, @)

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
    console.log("onEntriesReset")
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
  controller: null
  $el: null

  initialize: (controller) ->
    @controller = controller

  render: () ->
    layout.leftSidebar.html(
      @template(
        entryGroups: @entryGroups
      )
    )
    @$el = $('#entryGroupListContainer')
    @$el.find('.entryGroupRow').on("click", $.proxy(@onGroupClicked, @))

  onGroupClicked: (e) ->
    e.preventDefault()
    tgt = $(e.target).closest('.entryGroupRow')
    id = tgt.attr('data-id')
    @$el.find('.entryGroupRow').removeClass('selected')
    tgt.addClass('selected')
    @controller.setSelectedGroup(id)


class Application extends Backbone.Router
  views:
    entryList: null
    entryGroupList: null

  model:
    entries: null
    groups: null
    selectedGroupId: null

  routes:
    "" : "index"

  index: () ->
    @model.entries = new EntryCollection()
    @model.groups = new EntryGroupCollection()
    $.when(
      @model.entries.fetch(),
      @model.groups.fetch()
    ).done($.proxy(
      () ->
        @views.entryList = new EntryListView(@model.entries)
        @views.entryList.render()

        @views.entryGroupList = new EntryGroupListView(@)
        @views.entryGroupList.entryGroups = @model.groups
        @views.entryGroupList.render()
      @
      )
    )

  setSelectedGroup: (groupId) ->
    @model.selectedGroupId = groupId
    @model.entries.fetch(
      reset: true
      data:
        groupId: @model.selectedGroupId)

$ ->
  window.app = new Application()
  window.layout = new Layout()
  Backbone.history.start()