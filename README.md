# dj.plurality

## Updates

Decomplection

- plural-fns are now immutable values

- metadata is used extensively for extensibility

# Motivation

We want to solve the expression problem. Multimethods do so but with limitations. It has a locked resolving algorithm and complects state and extensibility.

Plural functions were coined [here](https://groups.google.com/forum/?fromgroups=#!topic/clojure/KC-zfUE1rXk), and I will refer to them as plural-fns. Plural-fns support any resolving algorithm and you can swap out resolving algorithm as you please (as long as you provide the transformation fn for implementations). Because plural-fns are immutable, they have less lookups than multimethods and do not implement references. This allows you to choose any reference type, like vars, atoms, or refs, to store and "mutate" the plural fn. Plural-fns are also just values making it easy to modify them like you would with hash-maps. Since they are just functions, you can create closures and dynamically generate them via higher-order functions.

# Core concepts

- A plural-fn is any fn that calls any number of methods from a collection of methods depending on the arguments to the plural-fn.

- An extensible-fn is a fn that supports:
 - modify-implementation
 - provides a way to access all the implementations

This is accomplished via metadata. All of this data is stored in the metadata of the plural-fn in the key, `:dj.plurality`

For example:

```lisp
(with-meta plural-fn
 (merge existing-metadata
        {:dj.plurality {:modify-implementations (fn ...)
                        :implementations [...]}}
```

dj.plurality provides helper functions to extract the information from the metadata, call the appropriate functions, and return the new plural-fn with updated implementations.

```lisp
(update-implementation plural-fn
 assoc
 [:triangle :square] (fn [x y] (println x y)))
```

### extensible-fn specification

An extensible function has metadata with key `:dj.plurality` mapped to a map that has keys `:modify-implementations` and `:implementations`.

`:modify-implementations` maps to a fn that accepts the current collection of implementations and returns a new collection of implementations.

`:implementations` maps to the plural-fn's current collection of implementations.

## Why use metadata instead of protocols?

I don't want to create a new type that implements IFn and all my protocols. I don't want to extend all fns to a new protocol. Maximum performance of the operation of extending the plural-fn is not an objective. The metadata is largely that, metadata, technically the plural-fn still functions as plural-fn even with the metadata removed. Extensibility is decomplected.

- plural-fn generators
 - to be flexible and efficient, the resolving algorithm will always be tied to plural-fn, thus we must delegate the majority of the work generating the plural-fn.
 - if performance is critical, once needs to be wary of arity, and one can overload on arity or use macros

dj.plurality will provide core implementations such as multimethods and predicate dispatch.

## Example Usage

```lisp
(require '[dj.plurality :as dp])
(let [x (dp/->simple-multi-fn {java.lang.Long inc}
                              type)
      y (dp/update-implementation x
                                  assoc
                                  java.lang.Double
                                  dec)]
     [(x 3)
      (y 3.0)
      (y 1)])
=> [4 2.0 2]
```
