(ns differentiae.progress.frontier
  "Tracks minimal sets of mutually incomparable elements of a partial order"
  (:require [differentiae.order.partial-order :as po]))

;; A set of mutually incomparable elements.
;;
;; An antichain is a set of partially ordered elements, each of which is incomparable to the others.
;; This antichain implementation allows you to repeatedly introduce elements to the antichain, and
;; which will evict larger elements to maintain the *minimal* antichain, those incomparable elements
;; no greater than any other element.
(defrecord AntichainImpl [elements]
  po/PartialOrder
  (less-than [this element]
    (boolean (some #(po/less-than % element) elements)))
  (less-equal [this element]
    (boolean (some #(po/less-equal % element) elements))))

(defn insert [ac element]  
  (if-not (some #(po/less-equal % element) (:elements ac))
    (-> ac
        (update :elements (partial filter #(not (po/less-equal element %))))
        (update :elements conj element))
    ac))

(defn insert!
  "Inserts an element into an atom containing an antichain. Returns true if the insert happened."
  [ac-atom element]
  (let [[before after] (swap-vals! ac-atom insert element)]
    (not= before after)))

(defn elements [ac-atom]
  (-> ac-atom deref :elements))

(defn antichain
  ([] (->AntichainImpl []))
  ([elems] (->AntichainImpl (seq elems))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Mutable Antichain
;;;;

(defprotocol MutableAntichain
  (clear! [ma])
  (empty! [ma])
  (update-dirty! [ma time delta])
  (frontier [ma] "Yields an immutable antichain of the minimal elements with positive count.")
  (is-empty? [ma])  
  
  (update-iter! [ma updates])
  (rebuild! [ma changes])
  (consolidate-updates! [ma])
  )

;; Todo: gross imperative mutable nonsense. Simplify, but keep efficient
;;       batching behavior?
(deftype MutableAntichainImpl [^:volatile-mutable dirty
                               ^:volatile-mutable updates
                               ^:volatile-mutable frontier
                               ^:volatile-mutable frontier-temp]
  MutableAntichain
  (clear! [this]
    (set! dirty 0)
    (set! updates (empty updates))
    (set! frontier (empty frontier))
    (set! frontier-temp (empty frontier-temp))
    this)

  (empty! [this]
    (set! updates (map (fn [update] (assoc update 1 0)) updates))
    (set! dirty (count updates))
    this)

  (update-dirty! [this time delta]
    (set! updates (conj updates [time delta]))
    (set! dirty (inc dirty))
    this)

  (frontier [this]
    (assert (= 0 dirty))
    (->AntichainImpl frontier))

  (is-empty? [this]
    (assert (= 0 dirty))
    (empty? frontier))
  
  (update-iter! [this new-updates]    
    (set! updates (into updates new-updates))
    (set! dirty (+ dirty (count new-updates)))

    (let [rebuild-required (atom false)]
      (while (and (> dirty 0) (not @rebuild-required))
        (let [[time delta] (nth updates (- (count updates) dirty))
              beyond-frontier (some #(po/less-than % time) frontier)
              before-frontier (not (some #(po/less-equal % time) frontier))]
          (reset! rebuild-required
                  #(or rebuild-required
                       (not (or beyond-frontier
                                (and (< delta 0) before-frontier)))))
          (set! dirty (dec dirty))))
      (set! dirty 0)
      (let [changes (cb/change-batch)]
        (if @rebuild-required
          (rebuild! this changes)
          changes))))

  (consolidate-updates! [this]    
    (let [grouped-updates (group-by first updates)
          consolidated-updates  
          (for [[time updates] grouped-updates
                :let [delta (reduce + (map second updates))]
                :when (not (zero? delta))]
            [time delta])]
      (set! updates consolidated-updates)))
  
  (rebuild! [this changes]    
    (consolidate-updates! this)

    ;; seed the reported changes with the absence of the current frontier
    (doseq [t frontier]
      (cb/update! changes t -1))

    ;; rebuild the frontier using the updates with positive occurrence     
    (let [new-frontier
          (reduce (fn [new-frontier [time delta]]                    
                    (if-not (some #(po/less-equal % time) new-frontier)
                      (conj new-frontier time)
                      new-frontier))
                  []
                  (sort (filter (comp pos? second) updates)))]

      (set! frontier new-frontier)
      
      ;; add in the new frontier elements
      (doseq [t new-frontier]
        (cb/update! changes t 1)))
    
    (remove (comp zero? second) @changes))

  po/PartialOrder
  (less-than [this element]
    (po/less-than (frontier this) element))
  (less-equal [this element]
    (po/less-than (frontier this) element)))

(defn mutable-antichain
  ([] (MutableAntichainImpl. 0 [] [] []))
  ([bottom-element] (MutableAntichainImpl. 0 [[bottom-element 1]] [bottom-element] [])))
