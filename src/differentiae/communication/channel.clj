(ns differentiae.communication.channel
  (:require [differentiae.communication.configuration :as cfg]
            [differentiae.communication :as comm]))

(defmulti channel
  "returns a pair of [Pusher Receiver] over a new channel
   for the provided configuration"
  ::cfg/config-type)

(defmethod channel ::cfg/process [_]

  )
