(ns differentiae.progress.nested.subgraph
  (:require [differentiae.progress.change-batch :as cb]
            [differentiae.progress.frontier :as f]
            [differentiae.progress.operate :as op]
            [differentiae.scheduling :as scheduling]))


(defrecord Subgraph [name
                     path
                     inputs
                     outputs
                     children
                     incomplete
                     incomplete-count
                     activations
                     temp-active
                     input-messages
                     output-capabilities
                     local-pointstamp
                     final-pointstamp
                     pointstamp-tracker
                     progcaster
                     shared-progress
                     scope-summary])


#_(defn schedule! [subgraph]
  (accept-frontier! subgraph)
  (harvest-inputs! subgraph)
  (recive-pointstamps! subgraph)
  (propagate-pointstamps! subgraph)
  
  )

