(ns mecca.mei
  (:require [goog.dom :as gdom]
            [goog.object :as o]
            [reagent.core :as r]
            ["node-xml-lite" :as xml]))

(def file-atom (r/atom "<section>
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

(defn file-upload []
  [:div
   [:h2 "Import .mei score"]
   [:input#input
    {:type      "file"
     :on-change (fn [e]
                  (let [dom    (o/get e "target")
                        file   (o/getValueByKeys dom #js ["files" 0])
                        reader (js/FileReader.)]
                    (.readAsText reader file)
                    (set! (.-onload reader)
                          #(reset! file-atom (-> % .-target .-result)))))}]])

(defn get-app-element []
  (gdom/getElement "app"))

(defn mei-out []
  [:textarea
   {:rows      30
    :cols      74
    :value     @file-atom
    ;:on-change #(reset! file-atom (-> % .-target .-result))
    :read-only true}])

(defn edn-out []
  [:div
   [:h2 "Converted to EDN:"]
   [:textarea
    {:rows      30
     :cols      74
     :value     (str (js->clj (.parseString xml @file-atom) :keywordize-keys true))

     :read-only true}]])

(def children (mapcat :childs))

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

(defn render-children []
  [:div
   [:h2 "Children:"]
   [:textarea
    {:rows      30
     :cols      74
     :value     (str (sequence children [(js->clj (.parseString xml @file-atom) :keywordize-keys true)]))

     :read-only true}]])

(comment
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
   [file-upload]
   [mei-out]
   [edn-out]
   [render-children]])

(defn mount [el]
  (r/render-component [mecca] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (js/console.log "start"))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (mount-app-element)
  (js/console.log "init")
  (start))

;; this is called before any code is reloaded
(defn ^:dev/before-load stop []
  (js/console.log "stop"))

(comment

  )