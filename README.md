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
To use this, make sure you add this somewhere in the render method of your page:

```Confirm.render()```

Once you do that, confirm methods are available anywhere as such:

```
      Confirm.confirm(
        header = Some("Delete thing"),
        question = "Are you Sure you want to delete this thing?",
        onConfirm = { () => 
            //Delete thing here
            // ...
            Toast.success("Successfully deleted the thing")
        }
      )
```

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

## net.leibman.react.Spinny

To use this, make sure you add this somewhere in the render method of your page:

```Spinny.render()```

Once you do that, you can turn spinny on or off:

```
//Start process
Spinny.on
//Do process
//..
Spinny.off
```

## net.leibman.react.calendar.Calendar