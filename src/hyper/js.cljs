(ns hyper.js
  (:require 
    [hyper.terse :refer-macros [js-iter]]
    [goog.style :as gs]))

'(predicates)

(defn element? [el] (= (.-nodeType el) 1))

'(utils)

(defn abs [n] (.abs js/Math n))

(defn log [v] (.log js/console v))

(defn put-local [k v] (.setItem (aget  js/window "localStorage") k v))

(defn get-local [k] (.getItem (aget  js/window "localStorage") k ))

(defn kw->str [k] (or (.-name k) (str k)))

(def ^:private js+2 (js* "function(a, b){return a + b;}"))
(defn js+ ([a] a) 
  ([a b] (js+2 a b))
  ([a b & more] (reduce js+2 a (cons b more))))

(def js= (js* "function(a, b){return a == b;}"))

(defn active-input []
  (if (#{"INPUT" "TEXTAREA"} (.. js/document -activeElement -tagName))
      (.. js/document -activeElement) nil))

(def html-encode (js*
"function htmlEncode( html ) {
    return document.createElement( 'a' ).appendChild( 
        document.createTextNode( html ) ).parentNode.innerHTML;};"))

(def html-decode (js*
"function htmlDecode( html ) {
    var a = document.createElement( 'a' ); a.innerHTML = html;
    return a.textContent;"))

'(objects)

(defn remove! [o k] (goog.object.remove o k))

(def locals 
  (js* "function(o){_o = new Array();for(k in o){if(o.hasOwnProperty(k)){_o.push(k);}}return _o;}"))

(def new-constructor 
  (js* "function(o, v){return new o['constructor'](v);}"))

(def new-prototype-constructor 
  (js* "function(o, v){return new o['prototype']['constructor'](v);}"))



'(arrays)

(defn array-remove [o v]
  (let [i (.indexOf o v)]
    (when (not= -1 i) (.splice o i 1) true)))

(defn array-unique-add [o v]
  (let [i (.indexOf o v)]
    (if (= -1 i) (.push o v))))

(defn array-concat [v o]
  (.apply (.. js/Array -prototype -concat) v, o))


'(style)

(defn camel-style 
  ([s] (camel-style (.createElement js/document "div") s))
  ([el s] (gs/getVendorJsStyleName_ el s)))

(defn ->style-sheet 
  ([el] (->style-sheet js/document el))
  ([document el]
    (let [sheets (.-styleSheets document)
          res (js-iter sheets sheet
            (if (= (.-ownerNode sheet) el) sheet))]
      (first (remove nil? res)))))

'(fanciful)

(defn ajax [url f & fail]
  (when-let [req (new js/XMLHttpRequest)]
    (set! (.-onreadystatechange req)
      (fn [e] 
        (if (= 4 (.-readyState req))
          (if (= 200 (.-status req)) 
            (f (.. e -target -response))
            (when (fn? (first fail)) ((first fail) e))))))
    (.open req "GET" url false)
    ;(.overrideMimeType req "text/xml; charset=iso-8859-1")
    (.send req)))


#_(defn resize-dataurl [data width height]
  (let [img (first ($ (str "<img src='" data "'>")))
        thumb (.createElement js/document "img")
        canvas (first ($ (str "<canvas width='" width "' height='" height "'></canvas>")))
        ctx (.getContext canvas "2d")]
    (.drawImage ctx img 0 0 width height)
    (set! (.-src thumb) (.toDataURL canvas "image/png"))
    ($/append (.-body js/document) thumb)
    (.toDataURL canvas "image/png")))


(defn file-drop-event [e read-k f]
  (let [files (js->clj (.-files (.-dataTransfer e)))
        length (.-length files)]
    (dorun (for [i (range 0 length) :let [file (aget files i)]]
      (let [reader (new js/FileReader)]
        (aset reader "onload" f)
        (case read-k
          :data-url (.readAsDataUrl reader file)
          (.readAsArrayBuffer reader file)))))))