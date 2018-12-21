(ns differentiae.scheduling)

(defprotocol Schedule    
  (path [x])
  (schedule! [x]))
