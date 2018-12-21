(ns differentiae.order.partial-order)

(defprotocol PartialOrder
  (less-than [this that])
  (less-equal [this that]))

(defprotocol TotalOrder)

(extend-protocol TotalOrder
  Long)

(extend-protocol PartialOrder
  Long
  (less-than [x y] (< x y))
  (less-equal [x y] (<= x y)))
