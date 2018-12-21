(ns differentiae.communication
  (:refer-clojure :exclude [send]))

(defprotocol Push
  (push! [pusher element]))

(defprotocol Pull
  (pull! [puller]))

(defn done [pusher]
  (push! pusher ::nil))
