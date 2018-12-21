(ns differentiae.dataflow.operators.generic.builder
  (:require [differentiae.progress.change-batch :as cb]
            [differentiae.progress.operate :as op]
            [differentiae.dataflow.scope :as df-scope]
            [differentiae.dataflow.operators.generic.operator-core :as op-core]
            [differentiae.dataflow.operators.capability :as cap]
            [differentiae.dataflow.channels.pushers.buffer :as buf]))

(defrecord OperatorBuilder [scope
                            index
                            global
                            address
                            shape
                            summary])

(defn operator-builder [scope name]
  (let [global (df-scope/new-identifier! scope)
        index (df-scope/allocate-operator-index! scope)
        address (conj (df-scope/address scope) index)
        peers (df-scope/peers scope)]
    (OperatorBuilder. scope index global address
                      (atom (op-core/operator-shape name peers))
                      (atom []))))

(defn new-input-connection* [] )

(defn new-input-connection [builder stream pact connection]
  
  )

(defn new-input! [builder stream pact])

(defn new-output! [builder])

(defn set-notify [builder val])

(defn operator-info [builder])

(defn build-operator! [builder f])

#_(defn build-operator!
  "Construct the operator defined by its op-data, push-external-progress!,
   and pull-internal-progress! functions and then add it to the scope."
  [operator-builder
   push-external-progress!
   pull-internal-progress!]
  (let [activations (activations scope)
        shared-progress (op/shared-progress (:inputs shape) (:outputs shape))
        operator (op-core/map->OperatorCore
                  (assoc operator-data
                         :activations activations
                         :push-external-progress! push-external-progress!
                         :pull-internal-progress! pull-internal-progress!
                         :shared-progress shared-progress))]
    (add-operator-with-indices! scope operator index global)))

