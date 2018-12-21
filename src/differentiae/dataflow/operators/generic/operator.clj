(ns differentiae.dataflow.operators.generic.operator
  (:require [differentiae.dataflow.operators.generic.builder :as b]
            [differentiae.dataflow.operators.generic.handles :as handles]
            [differentiae.dataflow.stream :as s]))

(defn new-source! [scope name constructor]
  (let [builder (b/operator-builder name scope)
        operator-info (b/operator-info scope)
        [output stream] (b/new-output! builder)
        builder (b/set-notify builder false)]
    (b/build-operator!
     (fn [capabilities]
       (let [capability (first capabilities)
             logic (constructor capability operator-info)]
         (fn [_frontier] (logic (handles/activate! output))))))))

(defn new-unary-operator! [stream pact name constructor]
  (let [builder (b/operator-builder name (s/scope stream))
        input (b/new-input! builder stream pact)
        [output stream] (b/new-output! builder)
        builder (b/set-notify builder false)        
        op-info (b/operator-info builder)]
    (b/build-operator!
     builder
     (fn [capabilities]       
       (let [capability (first capabilities)
             logic (constructor capability op-info)]
         (fn [_frontiers]
           (let [output-handle (handles/activate! output)]
             (logic input output-handle))))))
    stream))
