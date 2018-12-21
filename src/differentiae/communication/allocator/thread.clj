(ns differentiae.communication.allocator.thread
  (:require [differentiae.communication.allocator.protocols :as p]
            [differentiae.communication :as comm])
  (:import [java.util.concurrent ConcurrentLinkedQueue]
           [java.util Queue]))

(declare thread-comms)

(defrecord Pusher [^ Queue q]
  comm/Push
  (push! [this element]
    (.add q element)))

(defrecord Puller [current ^Queue q]
  comm/Pull
  (pull! [this]
    (locking current
      (let [cur (.poll q)]
        (vreset! current cur)
        cur))))

(defrecord ThreadComm []
  p/AllocateBuilder
  (build [this] this)
  p/Allocator
  (index [this] 0)
  (peers [this] 1)
  (pre-work! [this] nil)
  (post-work! [this] nil)
  (allocate [this identifier]
    (let [shared-q (ConcurrentLinkedQueue.)]
      [[(->Pusher shared-q)] (->Puller (volatile! nil) shared-q)])))

(def base-thread-comm (->ThreadComm))
(defn thread-comms [] base-thread-comm)
