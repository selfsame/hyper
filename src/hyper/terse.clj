(ns hyper.terse)

(def valid-attribute?
  #{'accept 'acceptCharset 'accessKey 'action 'allowFullScreen 'allowTransparency 'alt 'async 'autoComplete 'autoFocus 
  'autoPlay 'capture 'cellPadding 'cellSpacing 'challenge 'charSet 'checked 'classID 'className 'colSpan 'cols 'content 
  'contentEditable 'contextMenu 'controls 'coords 'crossOrigin 'data 'dateTime 'default 'defer 'dir 'disabled 'download 
  'draggable 'encType 'form 'formAction 'formEncType 'formMethod 'formNoValidate 'formTarget 'frameBorder 'headers 'height 
  'hidden 'high 'href 'hrefLang 'htmlFor 'httpEquiv 'icon 'id 'inputMode 'integrity 'is 'keyParams 'keyType 'kind 'label 
  'lang 'list 'loop 'low 'manifest 'marginHeight 'marginWidth 'max 'maxLength 'media 'mediaGroup 'method 'min 'minLength 
  'multiple 'muted 'name 'noValidate 'nonce 'open 'optimum 'pattern 'placeholder 'poster 'preload 'radioGroup 'readOnly 
  'rel 'required 'reversed 'role 'rowSpan 'rows 'sandbox 'scope 'scoped 'scrolling 'seamless 'selected 'shape 'size 'sizes 
  'span 'spellCheck 'src 'srcDoc 'srcLang 'srcSet 'start 'step 'style 'summary 'tabIndex 'target 'title 'type 'useMap 
  'value 'width 'wmode 'wrap 'onTouchMove 'onKeyDown 'onLoadedData 'onInput 'onChange 'onDragStart 'onDrop 'onContextMenu 
  'onClick 'onCut 'onTouchEnd 'onDrag 'onDragExit 'onDragEnter 'onTouchCancel 'onSubmit 'onTouchStart 'onBlur 'onWheel 
  'onHover 'onKeyUp 'onKeyPress 'onDragOver 'onDragLeave 'onDoubleClick 'onDragEnd 'onCopy 'onScroll 'onLoad 'onAbort 
  'onCanPlayThrough 'onDurationChange 'onEmptied 'onEncrypted 'onEnded 'onError 'onLoadedMetadata 'onLoadStart 'onPause 
  'onPlaying 'onProgress 'onRateChange 'onSeeked 'onSeeking 'onStalled 'onSuspend 'onTimeUpdate 'onVolumeChange 'onWaiting 
  'onMouseDown 'onMouseEnter 'onMouseLeave 'onMouseMove 'onMouseOut 'onMouseOver 'onMouseUp 'onCompositionEnd 'onFocus 
  'onPaste 'onCanPlay 'onPlay 'onSelect 'onCompositionStart 'onCompositionUpdate 'class 'key 'ref })

(defn attr-merge [a b]
  (cond (string? b) b
        (vector? b) (vec (concat a b))
        (sequential? b) (concat a b)
        :else (conj a b)))

(defn attr-post [m]
  (if (empty? (:class m)) (dissoc m :class)
      (assoc (dissoc m :class) :className 
        `(~'clj->js (apply str (interpose " " ~(:class m)))))))

(defn meta-use [form]
  (let [mm (mapv #(:_html (meta %)) form)]
    (if-not (first mm) form
      `(~(first form) 
        (~'clj->js ~(attr-post (apply merge-with attr-merge (filter map? mm))))
        ~@(filter #(not= {nil true} %) (rest form))))))
   
(defn -attr [form]
  (let [attr (first form)]
    (cond (= 'class attr) 
          (vec (rest form))
          (re-find #"^on[A-Z]" (str attr))
          (if (vector? (first (rest form)))
             `(fn ~@(rest form))
              (first (rest form))) 
          :else (first (rest form)))))

(defn soup [s]
  (when-let [tag (last (re-find #"\<([\w-_]+)" s))]
    (let [id (last (re-find #"\#([\w-_]+)" s))
          cs (mapv last (re-seq #"\.([\w-_]+)" s))]
    (with-meta (symbol (str "om.dom/" tag)) 
      {:_html (conj {} (if id {:id id}) (if (first cs) {:class cs}))}))))

(defn html-recur [form]
  (cond (symbol? form) (or (soup (str form)) form)
        (vector? form) (meta-use (mapv html-recur form))
        (list? form) (cond (valid-attribute? (first form))
                           (with-meta {nil true} {:_html {(keyword (first form)) (-attr form)}})
                           :else (meta-use (map html-recur form)))
        (map? form) form           
        :else form))
 
(defmacro html [& body]
  (let [forms (html-recur body)]
    `~(if (= 1 (count forms))
          (first forms)
          (concat ['om.dom/div (js-obj)] forms))))

'(fun stuff)

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