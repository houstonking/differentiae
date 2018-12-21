(ns differentiae.communication.initialize
  (:require [differentiae.communication.configuration :as cfg]
            [differentiae.communication.allocator.protocols :as alloc]
            [differentiae.communication.allocator.thread :as thread-allocator]
            [differentiae.communication.allocator.process :as process-allocator]
            #_[clojure.core.async :as ca])
  (:import [java.util.concurrent Executors]
           [java.lang Thread]))

(defrecord WorkerGuards [worker-handles others]
  clojure.lang.IDeref
  (deref [this]
    (doall (map deref worker-handles))))

;; produces a series of builders for allocators
(defmulti try-build! ::cfg/config-type)

(defmethod try-build! ::cfg/process [config]
  (let [num-threads (::cfg/num-threads config)]
    [(process-allocator/process-comms num-threads) nil]))

(defmethod try-build! ::cfg/thread [config]
  [[(thread-allocator/thread-comms)] nil])

(defn initialize-from [allocator-builders others logic]
  (let [guards (reduce-kv
                (fn [res i builder]
                  (conj res
                        (future (let [communicator (alloc/build builder)]
                                  (logic communicator)))))
                []
                allocator-builders)]
    (->WorkerGuards guards others)))

(defn initialize [config f]
  (let [[allocators others] (try-build! config)]
    (initialize-from allocators others f)))
