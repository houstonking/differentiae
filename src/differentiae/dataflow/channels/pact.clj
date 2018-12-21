(ns differentiae.dataflow.channels.pact
  (:require [differentiae.communication :as comm]
            [differentiae.communication.allocator.protocols :as alloc]
            [differentiae.communication.allocator.thread :as thread-alloc]))

(defprotocol ParallelizationContract
  (connect [pact allocator identifier logging] "Allocates a matched pair of push and pull endpoints implementing the pact."))

(defrecord LogPusher [pusher channel source target logging counter]
  comm/Push
  (push [_ msg]))

(defrecord LogPuller [puller channel index logging]
  comm/Pull
  (pull [_]))

(defrecord Pipeline []
  ParallelizationContract
  (connect [_ allocator identifier logging]
    ;; TODO: Why are we ignoring the allocator here?
    (let [[pusher puller] (alloc/allocate (thread-alloc/thread-comms) identifier)
          index (alloc/index allocator)]
      [(->LogPusher pusher index index identifier logging (atom 0))
       (->LogPuller puller index identifier logging)])))

(defrecord Exchange [f]
  ParallelizationContract
  (connect [_ allocator identifier logging]
    (let [[senders receiver] (alloc/allocate allocator identifier)

          ])
    ))
