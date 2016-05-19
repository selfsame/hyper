(ns hyper.test
  (:require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [cljs.pprint :as pprint]
    [hyper.terse :refer-macros [html]]
    [hyper.tools :as t]
    [hyper.js :as j]))

(enable-console-print!)

(def data1 {
  :foo {1 {:bar {:cat 6}}}
  :frog {:toad {:cat 1}
         :dog {:sam {:frodo -1} :foo {:a 1} :bar nil}}})

(def data2 {
  :foo {7 1}
  :frog {:toad {:dog 1}
         :dog {:sam {:pippen true} :foo {:b {:c 2}} :bar {:joe {1 9}}}}})




(deftest tools-predicates
  (is (= true 
    (t/solo? [1])
    (t/duo? {1 2 3 4})
    (t/triplet? #js [1 2 3])
    (t/quartet? #{1 2 3 4})
    (t/multiple? "joseph"))))

(deftest tools-maps
  (is (= 
    (t/combine data1 data2)
    {:foo {1 {:bar {:cat 6}} 7 1} 
     :frog {:toad {:cat 1 :dog 1} 
            :dog {:sam {:frodo -1 :pippen true} 
                  :foo {:a 1 :b {:c 2}} 
                  :bar {:joe {1 9}}}}} ))

  (is (= {0 [] 1 [1] 2 [1 2] 3 [1 2 3]}
      (t/map-by count [[][1][1 2][1 2 3]])))

  (is (= {:foo {7 1} :frog {:toad {:dog 1} 
          :dog {:foo {:b {:c 2}} :bar {:joe {1 9}}}}}
      (t/dissoc-in data2 [:frog :dog :sam :pippen])))

  (is (= {:frog {:toad {:dog 1}, :dog {:foo {:b {:c 2}}}}}
      (t/dissoc-all data2 
        [[:foo][:frog :dog :bar][:frog :dog :sam :pippen]])))

  (is (= {:a 1 :d 4}
      (t/dissoc-keys {:a 1 :b 2 :c 3 :d 4} [:b :c]))) )



; JS

(deftest js-utility

  (is (= '(false true false nil false) 
    (map j/element? [js/document (.-body js/document) js/window nil #{}])))

  (is (= '("joe" "joe" nil "6")
    (map j/kw->str [:joe "joe" nil 6])


)))

(run-tests)