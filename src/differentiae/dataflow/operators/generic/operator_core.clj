(ns differentiae.dataflow.operators.generic.operator-core
  (:require [differentiae.scheduling :as sched]
            [differentiae.progress.operate :as op]))

(defrecord OperatorShape [name
                          notify?
                          peers
                          inputs
                          outputs])

(defn operator-shape [name peers]
  (OperatorShape. name true peers 0 0))

(defrecord OperatorCore [shape
                         address
                         push-external-progress!
                         pull-internal-progress!
                         shared-progress
                         activations
                         summary]
  sched/Schedule
  (schedule! [x]
    (push-external-progress! (op/frontier shared-progress))
    (pull-internal-progress! (op/consumed shared-progress)
                             (op/internal shared-progress)
                             (op/produced shared-progress))))

