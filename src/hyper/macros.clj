(ns ^:figwheel-always hyper.macros)

(defmacro ? [& body]
  (let [[conds _ elses] (partition-by #(not= :else %) body)]
    (if elses
      `(~'cond ~@conds :else (~'do ~@elses))
      `(~'cond ~@conds))))

(defn- prop-symbol [k]
  (cond (string? k) (symbol (str ".-" k))
        (keyword? k) (symbol (apply str (cons ".-" (rest (str k)))))
        (symbol? k) (symbol (str ".-" k))
        :else k))

(defmacro ..! [& body]
  (let [value (last body)
        object (first body)
        path (map prop-symbol (butlast (rest body)))
        access (reduce (fn [form part] (list part form)) object path)]
    `(~'set! ~access ~value)))

(defmacro js-iter [ob binding & code]
  `(for [idx# (range (aget ~ob "length"))
         :let [~binding (aget ~ob idx#)]
         :when ~binding]
     (do ~@code)))