# Differentiae
A Clojure implementation of differential dataflows.


## Design Goals
1. (personal) Gain a working knowledge of differential dataflows and Rust.
2. Produce a Clojure transliteration of Frank McSherry's Rust library for [Differential Dataflows](https://github.com/frankmcsherry/differential-dataflow).
3. Explore the implications of differential dataflows backed by Clojure's persistent datastructures. (Are we perhaps able to exploit the datastructures to simplify concurrency-heavy parts of the implementation?)
