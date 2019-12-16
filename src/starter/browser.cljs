(ns starter.browser
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
   [:h2 "File upload"]
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
    :read-only true}])

(defn edn-out []
  [:textarea
   {:rows      30
    :cols      74
    :value     (str (js->clj (.parseString xml @file-atom) :keywordize-keys true))

    :read-only true}])

(defn mecca []
  [:div
   [:h1 "MECCA MEI"]
   [file-upload]
   [mei-out]
   [edn-out]])

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