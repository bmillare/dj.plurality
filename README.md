# dj.plurality

## New developments

Working on decomplecting current implementation

-Making plural-fns immutable

-Extensive use of metadata for extension points

# Motivation

We want to solve the expression problem. Multimethods do so but with limitations. It has a locked resolving algorithm and complects state and extensibility.

Plural functions were coined [here](https://groups.google.com/forum/?fromgroups=#!topic/clojure/KC-zfUE1rXk), and I will refer to them as plural-fns. Plural-fns support any resolving algorithm and you can swap out resolving algorithm as you please (as long as you provide the transformation fn for implementations). Because plural-fns are immutable, they have less lookups than multimethods and do not implement references. This allows you to choose any reference type, like vars, atoms, or refs, to store and "mutate" the plural fn. Plural-fns are also just values making it easy to modify them like you would with hash-maps. Since they are just functions, you can create closures and dynamically generate them via higher-order functions.

# Core concepts

- A plural-fn is any fn that calls any number of methods from a collection of methods depending on the arguments to the plural-fn.

- An extensible-fn is a fn that supports:
 - modify-implementation
 - and provides a way to access all the implementations

This is accomplished via metadata. All of this data is stored in the metadata of the plural-fn in the key, :dj.plurality

For example:

`(with-meta plural-fn (merge existing-metadata {:dj.plurality {:add-implementation (fn ...) ... :implementations [...]}}`

dj.plurality provides helper functions to extract the information from the metadata, call the appropriate functions, and return the correct value.

## Why use metadata instead of protocols?

I don't want to create a new type that implements IFn and all my protocols. I don't want to extend all fns to a new protocol. Absolute performance of extending fns is not an objective. The metadata is largely that, metadata, technically the plural-fn still functions as plural-fn even with the metadata removed. Extensibility is decomplected.

- plural-fn generators
 -to be flexible and efficient, the resolving algorithm will always be tied to plural-fn, thus we must delegate the majority of the work generating the plural-fn.
 -if you want you can use fn composition to generate the plural-fns, for arity you would need to call apply
 -if performance is critical, you could use macros and be explicit about arguments

dj.plurality will provide core implementations such as multimethods and predicate dispatch.