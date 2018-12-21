(ns differentiae.dataflow.operators.generic.handles
  (:require [differentiae.dataflow.operators.capability :as cap]
            [differentiae.dataflow.channels.pushers.buffer :as buf]))

;;;;;;;;;;;;;
;; HANDLES ;;
;;;;;;;;;;;;;

(deftype InputHandle [pull-counter
                      change-batches
                      logging]
  java.lang.Iterable
  (iterator [this] this)
  java.util.Iterator
  (next [_]
    (->> (next pull-counter)
         (map (fn [bundle]
                [(cap/capability-ref change-batches (:time bundle)) (:data bundle)])))))

(deftype OutputHandle [push-buffer change-batch])

(defn session [{:as output-handle :keys [push-buffer change-batch]}
               capability]
  (assert (cap/valid-for-output? capability change-batch))
  (buf/session push-buffer (:time capability)))

(defn activate! [output-handle])
