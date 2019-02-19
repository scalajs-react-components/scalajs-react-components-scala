# scalajs-react-components-scala

A repository of react components written in scala (i.e. not facades to javascript libraries)

So far we have the following 

##chandu0101.scalajs.react.components.DefaultSelect

##chandu0101.scalajs.react.components.Pager

##chandu0101.scalajs.react.components.ReactDraggable

##chandu0101.scalajs.react.components.ReactListView

##chandu0101.scalajs.react.components.ReactPopOver

##chandu0101.scalajs.react.components.ReachSearchBox

##chandu0101.scalajs.react.components.ReactTable

##chandu0101.scalajs.react.components.ReactTreeView

## net.leibman.react.Confirm

## net.leibman.react.Spinny

## net.leibman.react.Toast
To use this, make sure you add this somewhere in the render method of your page:

```Toast.render()```

Once you do that, the toast methods are available anywhere as such:

```
Toast.success(
  <.div("The action was successful!")
)
```

You can use success, error, warning, info or toast (this last one gives you flexibility to set the classname of the toast)

## net.leibman.react.calendar.Calendar