(ns mecca.mei
  (:require
   [goog.dom :as gdom]
   [goog.object :as o]
   [reagent.core :as r]
   ["node-xml-lite" :as xml]))

(defonce file-atom (r/atom "<section>
  <measure n=\"1\">
    <staff n=\"1\">
      <layer>
        <chord dur=\"1\">
          <note oct=\"5\" pname=\"c\"/>
          <note oct=\"4\" pname=\"g\"/>
          <note oct=\"4\" pname=\"e\"/>
        </chord>
      </layer>
    </staff>
    <staff n=\"2\">
      <layer>
        <note dur=\"1\" oct=\"3\" pname=\"c\"/>
      </layer>
    </staff>
  </measure>
</section>"))

(defn svg-out []
  [:div.svg {:dangerouslySetInnerHTML 
                   {:__html (.renderData js/vrvToolkit @file-atom)}}])

(defn file-upload []
  [:div
   [:h3 "Import .mei score"]
   [:input#input
    {:type      "file"
     :on-change (fn [e]
                  (let [dom    (o/get e "target")
                        file   (o/getValueByKeys dom #js ["files" 0])
                        reader (js/FileReader.)]
                    (.readAsText reader file)
                    (set! (.-onload reader)
                          #(reset! file-atom (-> % .-target .-result)))))}]])

(defn mei-out []
  [:div
   [:h3 "XML:"]
   [:textarea
    {:rows      15
     :cols      50
     :value     @file-atom
    ;:on-change #(reset! file-atom (-> % .-target .-result))
     :read-only true}]])

(defn edn-out []
  [:div
   [:h3 "EDN:"]
   [:textarea
    {:rows      15
     :cols      50
     :value     (str (js->clj (.parseString xml @file-atom) :keywordize-keys true))
     :read-only true}]])

(def children (mapcat :childs))

(def tags (map :name))

(defn tagp [pred]
  (comp children (filter (comp pred :name))))

(defn tag= [tag]
  (tagp (partial = tag)))

(defn attr-accessor [a]
  (comp a :attrib))

(defn attrp [a pred]
  (filter (comp pred (attr-accessor a))))

(defn attr= [a v]
  (attrp a (partial = v)))

(defn button [label onclick]
  [:button
   {:on-click onclick}
   label])

(def root (r/atom "root"))

(defn render-tags []
  (let [doc (js->clj (.parseString xml @file-atom) :keywordize-keys true)]
    [:div
     [:h3 (str "[" @root "]")]
     (for [tag (sequence tags [doc])]
       ^{:key tag}
       [button tag #(reset! root tag)])]))

(defn render-children []
  (let [doc (js->clj (.parseString xml @file-atom) :keywordize-keys true)]
    [:div
     [:textarea
      {:rows      15
       :cols      50
       :value     (str (sequence (tag= @root) [doc]))
       :read-only true}]]))

(comment

  (let [doc (js->clj (.parseString xml @file-atom) :keywordize-keys true)
        path [(tag= "section") children]]
    (tag= "mei") [doc])
  
  (sequence children [(js->clj (.parseString xml @file-atom) :keywordize-keys true)])
  (sequence (tag= "measure") [(js->clj (.parseString xml @file-atom) :keywordize-keys true)])
  
  (->> [(js->clj (.parseString xml @file-atom) :keywordize-keys true)]
       (sequence (tag= "measure"))
       count)
  
  (->> [(js->clj (.parseString xml @file-atom) :keywordize-keys true)]
       (sequence (comp (tag= :chapter)
                       (attr= :name "Conclusion")))
       count)
  
  )

(defn mecca []
  [:div
   [:h1 "MECCA MEI"]
   [:p "Music data browser"]
   [file-upload]
   [:p]
   [svg-out]
   [mei-out]
   [edn-out]
   ;[render-tags]
   ;[render-children]
   ])

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (r/render-component [mecca] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (mount-app-element)
  (js/console.log "start"))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (js/console.log "init")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))
