(ns differentiae.execute
  (:require [differentiae.communication.configuration :as cfg]
            [differentiae.communication.initialize :as init]
            [differentiae.worker :as wkr]))

(defn example
  "Executes a single-threaded dataflow execution from configuration."
  [f]
  (let [guards (init/initialize
                (cfg/thread-config)
                (fn [allocator]
                  (let [worker (wkr/worker allocator)
                        result (wkr/dataflow worker (fn [f] (f)))]
                    (while (wkr/step! worker))
                    result)))]
    @guards))
