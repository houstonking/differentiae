(ns differentiae.dataflow.operators
  (:require [differentiae.progress.change-batch :as cb]
            [differentiae.dataflow.scope :as s]
            [differentiae.dataflow.operators.capability :as cap]
            [differentiae.dataflow.operators.generic.operator :as operator]
            [differentiae.dataflow.operators.generic.handles :as handles]
            [differentiae.dataflow.channels.pushers.buffer :as buf]))

(defn map [logic stream]
  (operator/new-unary-operator!
   stream
   :pipeline
   "map"
   (fn constructor [_ _]
     (fn [input-handle output-handle]
       (doseq [[time data] input-handle]
         (-> (handles/session output-handle)
             (buf/give-iterator (map logic data))))))))

(defn activate! [activator])

(defn ->stream [data scope]
  (operator/new-source!
   scope
   "->stream"
   (fn constructor [capability operator-info]
     (let [activator (s/activator-for scope (:address operator-info))]
       (fn [output]
         ;; TODO: chunking?
         (-> (handles/session output)
             (buf/give-iterator data))
         (activate! activator))))))
