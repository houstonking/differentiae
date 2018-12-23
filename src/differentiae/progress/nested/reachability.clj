(ns differentiae.progress.nested.reachability
  (:require [differentiae.progress.frontier :as ft]
            [differentiae.progress.nested.subgraph :as sg])
  (:import [java.util.concurrent LinkedBlockingDeque]))

(defn builder [])

(defn add-node! [builder index inputs outputs summary])

(defn add-edge! [builder source target])

(defn build! [builder])
