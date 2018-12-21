(ns differentiae.dataflow.scopes)

(defprotocol Scope
  (name [scope]
    "A useful name describing the scope.")
  (addr [scope])
  (add-edge [scope soruce target]
    "A sequence of scope identifiers describing the path from the Root to this
     scope.")
  (allocate-operator-index! [scope]
    "Connects a source of data with a target of the data. This only links the two
     for the purposes of tracking progress, rather than effect any data movement
     itself.")
  (add-operator-with-indicies! [scope operator local global]
    "Allocates a new scope-local operator index.

     This method is meant for use with add_operator_with_index, which accepts a
     scope-local operator index allocated with this method. This method does
     cause the scope to expect that an operator will be added, and it is an
     error not to eventually add such an operator.")
  (scoped [scope f]
    "Creates a Subgraph from a closure acting on a Child scope, and returning
     whatever the closure returns.

     Commonly used to create new timely dataflow subgraphs, either creating new
     input streams and the input handle, or ingressing data streams and
     returning the egresses stream."))

#_(defn add-operator-with-index! [scope operator index]

  )

#_(defn add-operator!
  "Adds a child `Operate` to the builder's scope. Returns the new child's index."
  [scope operator]
  (let [index (allocate-operator-index! scope)
        global (new-identifier! scope)]
    (add-operator-with-indicies! scope operator index global)
    index))
