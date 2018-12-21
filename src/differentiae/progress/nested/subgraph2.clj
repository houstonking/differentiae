(ns differentiae.progress.nested.subgraph2
  (:require [differentiae.progress.frontier :as f]
            [differentiae.progress.change-batch :as cb]))



(defrecord Target [index port])
(defrecord Source [index port])

(defrecord ReachabilityBuilder
    [])

(defrecord SubgraphBuilder [;; a useful name for the subgraph
                            name 
                            ;; the index of the subgraph in its parent graph
                            index 
                            ;; a path of indexes that uniquely identifies this subgraph from a root
                            path 
                            ;; a vector of nodes in the subgraph (may be subgraphs themselves) 
                            children
                            ;; a vector of edges in the subgraph
                            edges])

(defrecord OperatorState [name
                          index
                          id
                          local
                          notify
                          inputs
                          outputs
                          recently-active
                          operator
                          edges
                          external
                          consumed-buffer
                          internal-buffer
                          produced-buffer
                          external-buffer
                          gis-capabilities
                          gis-summary
                          logging])

(defn empty-operator [logging]
  (map->OperatorState {:name "EmptyOperator"
                       :operator nil
                       :index 0
                       :id (Long/MAX_VALUE)
                       :local false
                       :inputs 0
                       :outputs 0
                       :recently-active true
                       :notify true
                       :edges []
                       :external []
                       :external-buffer []
                       :consumed-buffer []
                       :internal-buffer []
                       :produced-buffer []
                       :logging logging
                       :gis-capabilities []
                       :gis-summary []}))

(defn subgraph-builder [index parent-path logging]
  (let [my-path (conj parent-path index)
        parent-boundary-operator ()]
    (->SubgraphBuilder (str "Subgraph" my-path)
                       index
                       my-path
                       [parent-boundary-operator]
                       []
                       )))

(defn add-input  [operator-state]
  (-> operator-state
      (update :inputs inc)
      (update :extenal conj (f/mutable-antichain))
      (update :extneral-buffer conj (cb/change-batch))
      (update :consumed-buffer conj (cb/change-batch))))

(defn add-output [operator-state]
  (-> operator-state
      (update :outputs inc)
      (update :edges conj [])
      (update :internal-buffer conj (cb/change-batch))
      (update :produced-buffer conj (cb/change-batch))))


(defn new-input
  "Allocates a new input to the subgraph."
  [subgraph-builder shared-counts]
  (-> subgraph-builder
      (update :input-messages conj shared-counts)
      (update-in [:children 0] add-output)))

(defn new-output
  "Allocates a new output from the subgraph."
  [subgraph-builder]
  (-> subgraph-builder
      (update :output-capabilities conj (f/mutable-antichain))
      (update-in [:children 0] add-input)))
