(ns differentiae.progress.timestamp
  (:require [differentiae.order.partial-order :as po]))

(defprotocol PathSummary
  (results-in [this timestamp])
  (followed-by [this summary]))

(defrecord RootTimestamp []
  PathSummary
  (results-in [self timestamp] timestamp)
  (followed-by [self summary] summary)

  po/PartialOrder
  (less-equal [_ _] true))

;; nested pair of timestamps.
(defrecord Product [outer inner]
  PathSummary
  (results-in [this product])
  (followed-by [this product-summary])
  po/PartialOrder
  (less-equal [this that]
    (and (po/less-equal outer (:outer that))
         (po/less-equal inner (:inner that)))))

(defn product [outer inner] (->Product outer inner))

(defn root-timestamp [t] (product (->RootTimestamp) t))

(extend-type Long
  PathSummary
  (results-in [self ts] (+ self ts))
  (results-in [self summary] (+ self summary)))
