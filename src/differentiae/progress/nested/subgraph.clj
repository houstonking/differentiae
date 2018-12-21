(ns differentiae.progress.nested.subgraph
  (:require [differentiae.progress.change-batch :refer [change-batch]]
            [differentiae.progress.frontier :refer [mutable-antichain]]
            [differentiae.progress.operate :as op]))

;; Names a source of a data stream.
;;
;; A source of data is either a child output, or an input from a parent.
;; Conventionally, `index` zero is used for parent input.

;; index -- Index of the source operator.
;; port  -- Number of the output port from the operator.

(defrecord Source [index port])
(defn source [index port]
  (->Source index port))
;; Names a target of a data stream
;;
;; A target of data is either a child input, or an output to a parent.
;; Conventionally, `index` zero is used for parent output.

;; index -- Index of the target operator.
;; port  -- Number of the input port to the operator
(defrecord Target [index port])
(defn target [index port]
  (->Target index port))

(defrecord OperatorState
    [name
     index ;; index within parent scope
     id ;; worker-unique identifier
     local? ;; indicates whether the operator will exchange data or not
     notify?
     num-inputs
     num-outputs
     recently-active?
     operator
     edges ;; edges from the outputs of the operator
     external ;; [mutable-antichain] -- input capabilities expressed by outer scope
     consumed-buffer ;; [change-batch] ;; per-input: temp buffer used for pull-internal-progress
     internal-buffer ;;
     produced-buffer ;;
     external-buffer ;;
     gis-capabilities
     gis-summary
     logging])

(defn add-input [operator-state]
  (-> operator-state
      (update :num-inputs inc)
      (update :external conj (mutable-antichain))
      (update :external-buffer conj (change-batch))
      (update :consumed-buffer conj (change-batch))))

(defn add-output [operator-state]
  (-> operator-state
      (update :num-outputs inc)
      (update :edges conj [])
      (update :internal-buffer (change-batch))
      (update :produced-buffer (change-batch))))

(defn operator-state
  ([path logging]
   (map->OperatorState {}))
  ([scope index path identifier logging]
   (map->OperatorState
    {}
    )))

(defrecord Subgraph
    [name ;; human name
     path ;; vector of identifiers from the root
     num-inputs ;; number of inputs
     num-outputs ;; number of outputs

     children ;;handles to the children of the scope

     input-messages
     output-capabilities
     local-pointstamp-messages
     local-pointstamp-internal
     final-pointstamp-messages
     final-pointstamp-internal

     pointstamp-builder
     pointstamp-tracker

     prograster]

  op/Operate
  (name [_] name)
  (local? [_ ] false)
  (num-inputs [_] num-inputs)
  (num-outputs [_] num-outputs)

  (get-internal-summary [this]

    )
  )



(defprotocol SubgraphBuilder
  (new-input [sb shared-counts])
  (new-output [sb]))

(defn subgraph-builder [dataflow-index addr logging])

(defrecord SubgraphBuilderImpl [name
                                path
                                index
                                children
                                child-count
                                edge-stash
                                input-messages
                                output-capabilities])

(defn subgraph-builder [index path logging name]
  (map->SubgraphBuilderImpl
   {:name name
    :path (conj path index)    
    :child-count 0
    :edge-stash (atom [])
    :children (atom [(operator-state (conj path index) logging)])    
    :input-messages (atom (change-batch))
    :output-capabilities (atom (mutable-antichain))
    :logging logging}))









(defn build-subgraph [subgraph-builder worker]
  (let [num-inputs (-> subgraph-builder :input-messages count)
        num-outputs (-> subgraph-builder :output-capabilities count)
        ]
    
    )
  )
