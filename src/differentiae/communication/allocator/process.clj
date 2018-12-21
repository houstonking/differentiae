(ns differentiae.communication.allocator.process
  "Provides inter-thread intra-process channels"
  (:require [differentiae.communication.allocator.protocols :as p]
            [differentiae.communication.allocator.thread :as thread]
            [differentiae.communication :as comm])
  (:import [java.util.concurrent
            ConcurrentLinkedQueue
            ConcurrentHashMap]
           [java.util Queue]))

(defrecord ProcessComms [inner index peers channels]
  p/AllocateBuilder
  (build [this] this)
  p/Allocator
  (index [_] index)
  (peers [_] peers)
  (pre-work! [_] nil)
  (post-work! [_] nil)
  (allocate [this identifier]
    (let [push-queues channels
          pull-queue  (nth channels identifier)]
      ;; TODO: cache queue creation s.t. pushers / pullers aren't recreated?
      [(mapv thread/->Pusher push-queues)
       (thread/->Puller (volatile! nil) pull-queue)])))

(defn process-comms [count]
  ;;TODO: delay queue creation?
  (let [channels (into [] (repeatedly count #(ConcurrentLinkedQueue.)))]
    (mapv (fn [index]
            (map->ProcessComms
             {:inner (thread/thread-comms)
              :index index
              :peers count
              :channels channels}))
          (range count))))
