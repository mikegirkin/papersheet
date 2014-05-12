window.urls =
  entries: "/json/entries"
  entryGroups: "/json/entrygroups"

class Layout
  mainArea: $("#mainArea")
  leftSidebar: $("#leftSidebar")
  $header: $('.headerContainer')
  controller: null

  constructor: (controller) ->
    @controller = controller
    @$header.find('#showDone').click($.proxy(@toggleShowDone, @))
    @controller.model.applicationModel.on("change reset", @onApplicationModelChange, @)

  toggleShowDone: (e) ->
    e.preventDefault()
    @controller.toggleShowDone()

  onApplicationModelChange: () ->
    if @controller.model.applicationModel.get('showDone')
      @$header.find("#showDone").removeClass('doneNotShown')
      @$header.find("#showDone").addClass('doneShown')
    else
      @$header.find("#showDone").addClass('doneNotShown')
      @$header.find("#showDone").removeClass('doneShown')

class ValidatableView extends Backbone.View
  valid: () ->
    @$form.validate(@validationRules)
    @$form.valid()

class ApplicationModel extends Backbone.Model

class Entry extends Backbone.Model
  urlRoot: window.urls.entries

class EntryCollection extends Backbone.Collection
  model: Entry
  url: window.urls.entries

class EntryGroup extends Backbone.Model
  urlRoot: window.urls.entryGroups

class EntryGroupCollection extends Backbone.Collection
  model: EntryGroup
  url: window.urls.entryGroups

class EditEntryForm extends ValidatableView
  $el: null
  $shownInsteadOf: null
  $form: null
  controller: null
  modelBeingEdited: null
  isActive: false

  validationRules:
    rules:
      entryContent:
        minlength: 1
        required: true
    errorPlacement: (error, elem) ->
    highlight: (elem) ->
      $(elem).addClass('has-error')
    unhighlight: (elem) ->
      $(elem).removeClass('has-error')

  constructor: (controller) ->
    @controller = controller
    @$el = $('.editEntryForm')
    @$form = $('.editEntryForm')
    @$el.detach()

  edit: (el) ->
    if @isActive then @hide()
    @$shownInsteadOf = el
    @$shownInsteadOf.replaceWith(@$el)
    @$el.find('.okEditEntry').click(
      $.proxy(@onOkEditEntry, @))
    @$el.find('.cancelEditEntry').click(
      $.proxy(@onCancelEditEntry, @))
    editedEntryId = @$shownInsteadOf.attr('data-id')
    @modelBeingEdited = window.app.model.entries.get(editedEntryId)
    @$el.find('#entryContent').val(@modelBeingEdited.get('content'))
    @$el.find('#completed').prop('checked', @modelBeingEdited.get('stateId') == @controller.constants.closedStateId)
    @$el.find('input').focus()
    @isActive = true

  hide: () ->
    if !@isActive then return
    @$el.replaceWith(@$shownInsteadOf)
    @isActive = false

  onOkEditEntry: (e) ->
    e.preventDefault()
    if !@valid() then return
    content = @$el.find('#entryContent').val()
    @modelBeingEdited.set('content', content)
    stateId = if @$el.find('#completed').prop('checked') then @controller.constants.closedStateId else @controller.constants.openedStateId
    @modelBeingEdited.set('stateId', stateId)
    @modelBeingEdited.save().done(
      $.proxy(@hide, @)
    )

  onCancelEditEntry: (e) ->
    e.preventDefault()
    @hide()

class EntryListView extends ValidatableView
  template: _.template($("#entryListTemplate").html())
  entries: null
  controller: null
  $form: null
  validationRules:
    rules:
      newEntryContent:
        minlength: 1
        required: true
    errorPlacement: (error, elem) ->
    highlight: (elem) ->
      $(elem).addClass('has-error')
    unhighlight: (elem) ->
      $(elem).removeClass('has-error')

  initialize: (controller) ->
    @controller = controller
    @entries = @controller.model.entries
    @entries.on("reset change add remove", @onEntriesReset, @)

  render: () ->
    @controller.views.layout.mainArea.html(
      @template(
        entries: @entries
      )
    )
    @$el = $("#entryListViewContainer")
    @$form = @$el.find('#createEntryForm')
    @$el.find('#createNewEntryBtn').click($.proxy(@onCreateRequested, @))
    @$el.find(".changeEntryState").click($.proxy(@onChangeEntryStateRequested, @))
    @$el.on('click', '.editEntry', $.proxy(@onEditEntryRequested, @))

  onCreateRequested: (e) ->
    e.preventDefault()
    if(!@valid()) then return
    content = @$el.find("#newEntryContent").val()
    entry = new Entry(
      content: content
      stateId: 1
      groupId: @controller.model.applicationModel.get('selectedGroupId')
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
    entry.save().done($.proxy(() ->
        @render()
        @controller.refetch()
      @
    ))

  onEditEntryRequested: (e) ->
    e.preventDefault()
    $editedEl = $(e.target).closest('.entryRow')
    window.app.views.editEntryForm.edit($editedEl)

class EntryGroupListView extends Backbone.View
  template: _.template($("#entryGroupListTemplate").html())
  entryGroups: null
  applicationModel: null
  controller: null
  $el: null

  initialize: (controller) ->
    @controller = controller
    @applicationModel = @controller.model.applicationModel
    @entryGroups = @controller.model.groups
    @entryGroups.on("reset", @render, @)
    @applicationModel.on('change', @onSelectedGroupChanged, @)

  render: () ->
    @controller.views.layout.leftSidebar.html(
      @template(
        entryGroups: @entryGroups
        selectedGroupId: @applicationModel.get('selectedGroupId')
      )
    )
    @$el = $('#entryGroupListContainer')
    @$el.find('.entryGroupRow').on("click", $.proxy(@onGroupClicked, @))
    @$el.find('#addGroup').click($.proxy(@onAddGroupRequested, @))

  onGroupClicked: (e) ->
    e.preventDefault()
    tgt = $(e.target).closest('.entryGroupRow')
    id = tgt.attr('data-id')
    @controller.setSelectedGroup(id)

  onSelectedGroupChanged: () ->
    groupId = @applicationModel.get('selectedGroupId')
    @$el.find('.entryGroupRow').removeClass('selected')
    @$el.find('.entryGroupRow[data-id="' + groupId + '"]').addClass('selected')

  onAddGroupRequested: (e) ->
    group = new EntryGroup()
    @controller.views.editGroupForm.edit(group)
    e.preventDefault()

class EditGroupForm extends ValidatableView
  el: '#editGroupForm'
  $input: null
  $form: null
  controller: null
  group: null

  validationRules:
    rules:
      groupName:
        minlength: 1
        required: true
    errorPlacement: (error, elem) ->
    highlight: (elem) ->
      $(elem).addClass('has-error')
    unhighlight: (elem) ->
      $(elem).removeClass('has-error')

  initialize: (controller) ->
    @controller = controller
    @$el.find('#closePopup').click($.proxy(@onCancel, @))
    @$el.find('#okEditGroup').click($.proxy(@onOk, @))
    @$el.find('#cancelEditGroup').click($.proxy(@onCancel, @))
    @$form = @$el.find('form')
    @$input = @$el.find('#groupName')

  edit: (group) ->
    @group = group
    @$el.show()
    @$input.val('')
    @$input.focus()

  hide: () ->
    @$el.hide()

  onOk: (e) ->
    e.preventDefault()
    if (!@valid()) then return
    name = @$input.val()
    @group.set('name', name)
    @group.save().done($.proxy(() ->
        @controller.refetchGroups()
        @$el.hide()
      @
    ))

  onCancel: (e) ->
    @$el.hide()
    e.preventDefault()


class Application extends Backbone.Router
  constants:
    openedStateId: 1
    closedStateId: 2

  views:
    entryList: null
    entryGroupList: null
    editEntryForm: null
    editGroupForm: null
    layout: null

  model:
    entries: null
    groups: null
    applicationModel: null

  routes:
    "" : "index"

  initialize: () ->
    @model.applicationModel = new ApplicationModel()
    @model.applicationModel.set("showDone", false)
    @views.editEntryForm = new EditEntryForm(@)
    @views.editGroupForm = new EditGroupForm(@)
    @views.layout = new Layout(@)

  index: () ->
    @model.entries = new EntryCollection()
    @model.groups = new EntryGroupCollection()
    @refetchGroups().done($.proxy(
      () ->
        @views.entryGroupList = new EntryGroupListView(@)
        @views.entryGroupList.render()

        @views.entryList = new EntryListView(@)

        @setSelectedGroup(@model.groups.first().get('id'))
      @
      )
    )

  setSelectedGroup: (groupId) ->
    @model.applicationModel.set('selectedGroupId', groupId)
    @refetch()

  refetch: () ->
    request = {
      reset: true
      data: {}
    }
    if(not @model.applicationModel.get('showDone')) then request.data.stateId = @constants.openedStateId
    if(@model.applicationModel.get('selectedGroupId') != null) then request.data.groupId = @model.applicationModel.get('selectedGroupId')
    @model.entries.fetch(request)

  refetchGroups: () ->
    @model.groups.fetch({reset: true})

  toggleShowDone: () ->
    @model.applicationModel.set('showDone', not @model.applicationModel.get('showDone'))
    @refetch()

$ ->
  window.app = new Application()
  Backbone.history.start()

