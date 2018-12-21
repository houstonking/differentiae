(ns differentiae.dataflow.operators.generic.builder-test
  (:require [clojure.test :refer :all]            
            [differentiae.dataflow.operators.generic.builder :refer :all]
            [differentiae.execute :refer [example]]))


(deftest incorrect-capabilities
  ;; This tests that if we attempt to use a capability associated
  ;; with the wrong output we get a run-time exception
  (example
   (fn [scope]
     (let [builder (operator-builder "Incorrect Capabilities Test" scope)
           [builder output-1 stream-1] (new-output builder)
           [builder output-2 stream-2] (new-output builder)]
       (build builder
              (fn [capabilities]
                (fn [frontiers]
                  (let [output-handle-1 (activate output-1)
                        output-handle-2 (activate output-2)]
                    ;; pass the wrong capability
                    (session output-handle-2 (nth capabilities 0))))))))))
