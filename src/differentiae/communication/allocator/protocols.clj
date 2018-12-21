(ns differentiae.communication.allocator.protocols)

(defprotocol Allocator
  (index [allocator] "The index of the worker out of (0..peers).")
  (peers [allocator] "The number of workers.")
  (pre-work! [allocator] "Perform work before scheduling operators.")
  (post-work! [allocator] "Perform work after scheduling operators.")
  (allocate [allocator n] "Constructs several send endpoints and one receive endpoint. Returns a vector [[sender-i] receiver]"))

(defprotocol AllocateBuilder
  (build [this]
    "A proto-allocator, which implements Send and can be completed with build.

     This trait exists because some allocators contain non-Send elements, like
     Rc wrappers for shared state. As such, what we actually need to create to
     initialize a computation are builders, which we can then spawn in new
     threads each of which then construct their actual allocator."))
