(ns clojure.core.logic.interp
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic))

(def ^:dynamic *trees* nil)

(defn unwrap [g]
  (cond
   (trace-subst? g) g
   (= (mk-exp g) :inc) (recur (g))
   :else  g))

(declare tracer trace-subst?)

(defprotocol ISearchTree
  (tree [this]))

(deftype TraceSubstitutions [_tree]
  ISearchTree
  (tree [this] _tree)

  IBind
  (bind [this g]
    (let [exp (mk-exp g)
          sub-tree (cond
                    (= exp :fresh) [exp (mk-vars g)]
                    (= exp :conde) []
                    :else :goal)
          sub-s (tracer sub-tree)
          new-tree (if (= exp :conde)
                     (conj _tree [:conde (tree (unwrap (g sub-s)))])
                     (conj _tree (tree (unwrap (g sub-s)))))]
      (tracer new-tree)))

  IMPlus
  (mplus [this f]
    (let [s (unwrap (f))]
      (tracer (into [_tree] [(tree s)])))))

(defn tracer
  ([] (tracer []))
  ([tree]
     (TraceSubstitutions. tree)))

(defn trace-subst? [x]
  (instance? TraceSubstitutions x))

(comment
  ;; works
  (tree (bind (bind (tracer) s#) s#))

  ;; works
  (tree (bind (tracer) (fresh [x] s# s#)))

  ;; works
  (tree (bind (tracer) (fresh [x] (fresh [y] s#) s#)))

  ;; mplus?
  (tree
   (bind (tracer)
         (fresh [x]
           (conde
             [s# s#]
             [s# s#])
           s#)))

  ;; we don't want conde copied into it
  
  ;; 1. we care about fresh
  ;; 2. we care about conde
  ;; 3. if encounter -inc, we force it
  ;; 4. we never call goals we've seen before
  ;; 5. 

  ;; we 
  )