(ns differentiae.examples.hello
  (:require [differentiae.protocols :as p]
            [differentiae.dataflow :refer [new-input-handle new-probe-handle]]
            [differentiae.dataflow.operators :as ops]))

(defn hello-example [args]
  (execute-from-args
   args
   (fn [worker]
     (let [index (p/index worker)
           input (new-input-handle)
           probe (new-probe-handle)]

       ;; create a new input, exchange data, and inspect its output
       (p/dataflow
        worker
        (fn [scope]
          (-> scope
              (ops/input-from input)
              (ops/exchange identity)
              (ops/inspect (fn [x] (println "Worker" index "hello" x)))
              (ops/probe-with probe))))



     ))))
