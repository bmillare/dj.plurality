(ns dj.plurality)

;; some decisions that were made in this code that could probably be
;; made more extensible in the future

;; 1 - atoms are used instead of refs

;; 2 - vectors are used instead of any arbitrary datastructure

;; 3 - how redefining is implemented, maybe we want defplural to
;; overwrite each time, and sometimes we want to switch the resolver.

;; Future optimization, using a caching strategy for specific
;; implementations of resolver.

;; Future optimization, compile an optimized pluralfn based on
;; implementations. This would rely on heavy use of metadata.

(defn save-implementations [pluralfn-name empty-data]
  (if (resolve pluralfn-name)
    `(or @(:dj.plurality/implementations (meta ~pluralfn-name))
	 ~empty-data)
    empty-data))

(defmacro defplural-body [pluralfn-name arg-list & resolver-body]
  `(def ~pluralfn-name (let [implementations# (atom ~(save-implementations pluralfn-name []))]
			 (with-meta (fn ~pluralfn-name ~(into [] (rest arg-list))
				      (let [~(first arg-list) @implementations#]
					~@resolver-body))
			   {:dj.plurality/implementations implementations#}))))

(defmacro defplural [pluralfn-name resolver]
  `(defplural-body ~pluralfn-name [implementations# & args#]
     (~resolver implementations# args#)))

(defn clear-implementations! [plural-fn]
  (reset! (:dj.plurality/implementations (meta plural-fn))
	  []))

(defn defimplementation [plural-fn implementation]
  (swap! (:dj.plurality/implementations (meta plural-fn))
	 conj
	 implementation))

(defn cumulative [implementations args]
  (apply (apply comp implementations) args))

(defn clobber [implementations args]
  (-> implementations peek (apply args)))

(defn broadcast [implementations args]
  (->> implementations (map #(apply % args)) dorun))

(defn all [implementations args]
  (->> implementations (map #(apply % args)) set))

(defn predicate [implementations args]
  (let [match (->> implementations (filter (fn [[pred? implementation]] (apply pred? args))) first)]
    (apply match args)))

(defn multi [implementations args]
  (let [dispatch identity
	implementation ((reduce into implementations) (apply dispatch args))]
    (apply implementation args)))

(defn random [implementations args]
  (-> implementations rand-nth (apply args)))