(ns differentiae.examples.to-stream
  (:require [differentiae.execute :refer [example]]
            [differentiae.dataflow.operators :refer [inspect]]
            [differentiae.dataflow.operators.generic.operator :refer [source]]))


(defn simple-example []
  (example
   (fn [scope]
     (let [])
     )))
