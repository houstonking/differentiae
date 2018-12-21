(ns differentiae.dataflow.channels
  (:require [differentiae.communication :as comm]))

(defrecord Message [time data from seq-num])

(defn message [time data from seq-num]
  (Message. time data from seq-num))

(defn push-at! [pusher time data]
  (let [msg (message time data 0 0)]
    (comm/push! pusher msg)))
