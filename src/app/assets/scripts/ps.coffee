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

class EditEntryForm
  $el: null
  $shownInsteadOf: null
  modelBeingEdited: null

  constructor: () ->
    @$el = $('.editEntryForm')
    @$el.detach()

  edit: (el) ->
    @$shownInsteadOf = el
    @$shownInsteadOf.replaceWith(@$el)
    @$el.find('.okEditEntry').click(
      $.proxy(@onOkEditEntry, @))
    @$el.find('.cancelEditEntry').click(
      $.proxy(@onCancelEditEntry, @))
    editedEntryId = @$shownInsteadOf.attr('data-id')
    @modelBeingEdited = window.app.model.entries.get(editedEntryId)
    @$el.find('#entryContent').val(@modelBeingEdited.get('content'))
    @$el.find('input').focus()

  hide: () ->
    @$el.replaceWith(@$shownInsteadOf)

  onOkEditEntry: (e) ->
    e.preventDefault()
    content = @$el.find('#entryContent').val()
    @modelBeingEdited.set('content', content)
    @modelBeingEdited.save().done(
      $.proxy(@hide, @)
    )

  onCancelEditEntry: (e) ->
    e.preventDefault()
    @hide()

class EntryListView extends Backbone.View
  template: _.template($("#entryListTemplate").html())
  entries: null
  controller: null

  initialize: (controller) ->
    @controller = controller
    @entries = @controller.model.entries
    @entries.on("reset change add remove", @onEntriesReset, @)

  render: () ->
    layout.mainArea.html(
      @template(
        entries: @entries
      )
    )
    @$el = $("#entryListViewContainer")
    @$el.find('#createNewEntryBtn').click($.proxy(@onCreateRequested, @))
    @$el.find(".changeEntryState").click($.proxy(@onChangeEntryStateRequested, @))
    @$el.on('click', '.editEntry', $.proxy(@onEditEntryRequested, @))

  onCreateRequested: (e) ->
    e.preventDefault()
    content = @$el.find("#newEntryContent").val()
    entry = new Entry(
      content: content
      stateId: 1
      groupId: window.app.model.selectedGroupId
    )
    entry.save().done($.proxy(
      () -> @controller.refetch()
      @
    ))

  onEntriesReset: () ->
    @render()

  onChangeEntryStateRequested: (e) ->
    e.preventDefault()
    entryId = $(e.target).closest(".entryRow").attr('data-id')
    entry = @entries.get(entryId)
    entry.set('stateId', window.app.constants.closedStateId)
    entry.save().done(
      $.proxy(@render, @)
    )

  onEditEntryRequested: (e) ->
    e.preventDefault()
    $editedEl = $(e.target).closest('.entryRow')
    window.app.views.editEntryForm.edit($editedEl)

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
  constants:
    openedStateId: 1
    closedStateId: 2

  views:
    entryList: null
    entryGroupList: null
    editEntryForm: null

  model:
    entries: null
    groups: null
    selectedGroupId: null

  routes:
    "" : "index"

  initialize: () ->
    @views.editEntryForm = new EditEntryForm()

  index: () ->
    @model.entries = new EntryCollection()
    @model.groups = new EntryGroupCollection()
    $.when(
      @model.entries.fetch(),
      @model.groups.fetch()
    ).done($.proxy(
      () ->
        @views.entryList = new EntryListView(@)
        @views.entryList.render()

        @views.entryGroupList = new EntryGroupListView(@)
        @views.entryGroupList.entryGroups = @model.groups
        @views.entryGroupList.render()
      @
      )
    )

  setSelectedGroup: (groupId) ->
    @model.selectedGroupId = groupId
    @refetch()

  refetch: () ->
    request = { reset: true }
    if(@model.selectedGroupId != null) then request.data = { groupId: @model.selectedGroupId }
    @model.entries.fetch(request)


$ ->
  window.app = new Application()
  window.layout = new Layout()
  Backbone.history.start()