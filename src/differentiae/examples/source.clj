(ns differentiae.examples.source
  (:require [differentiae.execute :refer [example]]
            [differentiae.dataflow.operators :refer [inspect]]
            [differentiae.dataflow.operators.generic.operator :refer [source]]))


(defn simple-example []
  (example
   (fn [scope]
     (source scope "Source"
             (fn [capability]
               (fn [output]
                 (let [done (atom false)]
                   (when-let [cap capability]
                     (-> output
                         (session cap))
                     )
                   )
                 )

               ))
     )))
