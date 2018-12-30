(ns differentiae.progress.timestamp
  (:require [differentiae.order.partial-order :as po]))

(defprotocol PathSummary
  (results-in* [this timestamp])
  (followed-by* [this summary]))

(defrecord RootTimestamp []
  PathSummary
  (results-in* [self timestamp] timestamp)
  (followed-by* [self summary] summary)

  po/PartialOrder
  (less-equal [_ _] true))

;; nested pair of timestamps.
(defrecord Product [outer inner]
  PathSummary
  (results-in* [this product])
  (followed-by* [this product-summary])
  po/PartialOrder
  (less-equal [this that]
    (and (po/less-equal outer (:outer that))
         (po/less-equal inner (:inner that)))))

(defn product [outer inner] (->Product outer inner))

(defn root-timestamp [t] (product (->RootTimestamp) t))

(deftype DefaultSummary []
  po/PartialOrder
  (less-equal [_ _] true))

(def default-summary (DefaultSummary.))
(defn default-summary? [x] (instance? DefaultSummary x))

#_(extend-type DefaultSummary
  PathSummary
  (results-in* [_ ts] ts)
  (followed-by* [_ summary] summary))

(extend-type Long
  PathSummary
  (results-in* [self ts] (+ self ts))
  (followed-by* [self summary] (+ self summary)))


(defn results-in [ts1 ts2]  
  (cond
    (and (default-summary? ts1)
         (default-summary? ts2)) default-summary

    ;; TODO: is identity on default a valid behavior?
    (default-summary? ts1) ts2
    (default-summary? ts2) ts1
    
    :else (results-in* ts1 ts2)))

(defn followed-by [s1 s2]  
  (cond
    (and (default-summary? s1)
         (default-summary? s2)) default-summary

    ;; TODO: is identity on default a valid behavior?
    (default-summary? s1) s2
    (default-summary? s2) s1
    
    :else (results-in* s1 s2)))
