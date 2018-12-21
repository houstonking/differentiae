(ns differentiae.dataflow.stream)

(defn scope [stream])

#_(defrecord StreamImpl [name ;; the progress identifier of the stream's data source
                       ports ;; maintains a list of push interested in the streams output
                       scope ;; the scope containing the stream
                       ])

#_(defn stream [source output scope]
  (->StreamImpl source output scope))

#_(defn connect-to [stream target pusher identifier]
  ;; TODO: Logging.
  (add-edge! scope name target)

  )
