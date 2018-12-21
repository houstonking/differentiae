(ns differentiae.examples.simple
  (:require [differentiae.execute :refer [example]]
            [differentiae.dataflow.operators :refer [to-stream inspect]]))


(defn simple-example []
  (example
   (fn [scope]
     (-> (to-stream scope (range 10))
         (inspect (fn [x] (println "seen:" x)))))))

(defn simple-example-deconstructed []
  (example
   

   ))
