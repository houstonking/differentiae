(ns differentiae.worker
  (:require [differentiae.communication.allocator.protocols :as p]
            [differentiae.progress.nested.subgraph :as sg]
            [differentiae.progress.operate :as op]
            [differentiae.dataflow.scopes.child :refer [map->Child]])
  (:import [java.time Instant]))

;; A `Worker` is the entry point to a timely dataflow computation. It wraps a `Allocate`,
;; and has a list of dataflows that it manages.

(defn step-dataflow! [dataflow]
  (let [active? (op/pull-internal-progress! dataflow (atom {:consumed []
                                                         :internal []
                                                         :produced []}))]
    active?))

(defn step-worker!
  "Steps the worker, giving all of its dataflows a chance to execute.
   Returns the worker with the :active? flag set if it still has an
   active dataflow."
  [{:as worker :keys [allocator dataflows]}]
  (p/pre-work! allocator)
  (let [active? (reduce
                 (fn [active? dataflow]
                   (let [sub-active? (step-dataflow! dataflow)]
                     (or active? sub-active?)))
                 false
                 dataflows)]
    (p/post-work! allocator)
    (-> worker
        #_(remove-inactive-dataflows)
        (assoc :active? active?))))

  
  
  
  
  
  
  

  
  
  
