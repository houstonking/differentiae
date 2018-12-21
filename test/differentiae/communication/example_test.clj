(ns differentiae.communication.example-test
  (:require [clojure.test :refer :all]
            [differentiae.communication :as comm]
            [differentiae.communication.allocator.protocols :as alloc]
            [differentiae.communication.initialize :as init]
            [differentiae.communication.configuration :as config]
            [differentiae.communication.message :as msg]
            ))

#_(deftest example
  (let [conf (config/process-config 2)tempus
        guards (init/initialize conf
                           (fn [allocator]
                             (println "worker" (alloc/index allocator) "started")
                             (let [[[sender-0 sender-1] receiver] (alloc/allocate allocator 0)]
                               (send* sender-0 (msg/message (format "hello %s" 0)))
                               (send* sender-1 (msg/message (format "hello %s" 1)))
                               (let [expecting (atom 2)]
                                 (while (pos? @expecting)
                                   (alloc/pre-work! allocator)
                                   (when-let [msg (recv receiver)]
                                     (println "worker" (alloc/index allocator)
                                              "got message:" @msg)
                                     (swap! expecting dec))
                                   (alloc/post-work! allocator))))
                             (alloc/index allocator)))]

    (if (ok? guards)
      (doseq [guard @guards]
        (println "result:" guard))
      (println "error in initializing"))))


(defn test-for-config [config]
  (let [guards
        (init/initialize
         config
         ;; function to run on each worker. input is an allocator for communication.
         (fn [allocator]
           (let [index (alloc/index allocator)
                 peers (alloc/peers allocator)]

             (println (format "starting worker %s of %s" (inc index) peers))

             ;; allocate our communication channels:
             ;; a vector of queues out to each peer
             ;; a single queue in to us
             (let [[senders receiver] (alloc/allocate allocator index)
                   recv-count (atom 0)]

               ;; send a message out to everyone else.
               (doseq [i (range peers)]
                 (comm/push (nth senders i) (format "hello, %s [from %s]" i index)))

               ;; loop until we've heard from everyone
               (while (< @recv-count peers)
                 (alloc/pre-work! allocator)
                 (when-let [msg (comm/pull receiver)]
                   (println (format  "worker %s received: %s" index msg))
                   (swap! recv-count inc))
                 (alloc/post-work! allocator)))
             ;; return something.
             index)))]

    (doseq [guard @guards]
      (println "result:" guard))))
