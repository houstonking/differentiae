(ns differentiae.communication.configuration)

(defn thread-config []
  {::config-type ::thread})

(defn process-config [n]
  {::config-type ::process
   ::num-threads n})
