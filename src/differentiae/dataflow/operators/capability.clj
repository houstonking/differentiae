(ns differentiae.dataflow.operators.capability
  (:require [differentiae.order.partial-order :as po]
            [differentiae.progress.change-batch :as cb])
  (:refer-clojure :exclude [time]))

(defrecord Capability [time change-batch]
  po/PartialOrder
  (less-equal [this other]
    (and (po/less-equal time (:time other))
         (identical? change-batch (:change-batch other)))))

(defn time [cap] (:time cap))
(defn valid-for-output? [cap])

(defn mint! [change-batch time]
  (swap! change-batch cb/update time 1)
  (Capability. time change-batch))

(defn delayed [{:as cap :keys [time change-batch]} new-time]
  (assert (po/less-equal time new-time)
          "Cannot delay when time is not less-equal capabilities time")
  (mint! change-batch time))

(defn drop [{:as cap :keys [time change-batch]}]
  (swap! change-batch cb/update time -1))


(defrecord CapabilityRef [time change-batches])

(defn capability-ref
  "Yields a capability which has not yet been incorporated into the
   change batch. Calling `retain!` will yield a capability which has been
   noted in the change batch. Any such claimed capability *must* be
   explicitly `drop`'ed in order to ensure that the progress tracking
   machinery continues to function."
  [change-batches time]
  (CapabilityRef. time change-batches))

(defn delayed-for-output [{:as cap-ref :keys [time change-batches]}
                          new-time output-port]
  (assert (po/less-equal time new-time))
  (assert (< output-port (count change-batches)))
  (mint! new-time (nth change-batches output-port)))

(defn retain-for-output! [{:as cap-ref :keys [time change-batches]}
                          output-port]
  (assert (< output-port (count change-batches)))
  (mint! time (nth change-batches output-port)))

