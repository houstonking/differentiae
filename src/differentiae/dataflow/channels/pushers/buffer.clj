(ns differentiae.dataflow.channels.pushers.buffer
  "Buffering and session mechanisms to provide the appearance of
   record-at-a-time sending, with the performance of batched sends."
  (:require [differentiae.dataflow.channels :as chan]))

(defrecord Session [buffer])

(defrecord Buffer [time buffered-data pusher])

(defn buffer [pusher]
  (atom (Buffer. nil [] pusher)))

(defn flush-buffer [{:as buffer :keys [buffered-data time pusher]}]
  (if (not-empty buffered-data)
    (do (chan/push-at! pusher buffered-data time)
        (assoc buffer :buffered-data []))
    buffer))

(defn give-buffer [buffer data]
  (update buffer :buffered-data conj data))


;; TODO: are agents a bad pattern here?
(defn session [{:as buffer} time]
  (Session. (agent (assoc buffer :time time))))

(defn give-iterator [{:as session :keys [buffer]} iterator]
  (doseq [i iterator] (send buffer give-buffer i)))
