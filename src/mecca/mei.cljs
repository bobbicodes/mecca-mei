(ns mecca.mei
  (:require [clojure.spec.alpha :as s]
            [goog.dom :as gdom]
            [goog.object :as o]
            [reagent.core :as r]
            ["node-xml-lite" :as xml]
            [clojure.edn :as edn]
            [mecca.hiccup :as hiccup]))

(def mei-header "<mei xmlns=\"http://www.music-encoding.org/ns/mei\">
    <meiHead>
        <fileDesc>
            <titleStmt>
                <title/>
            </titleStmt>
            <pubStmt/>
        </fileDesc>
    </meiHead>
    <music>
        <body>
            <mdiv>
                <score>
                    <scoreDef>
                        <staffGrp>
                            <staffDef clef.shape=\"G\" clef.line=\"2\" n=\"1\" lines=\"5\"/>
                        </staffGrp>
                    </scoreDef>
                    <section>
                        <measure>
                            <staff n=\"1\">
                                <layer>")             
                                                  
(def mei-footer "</layer>
                            </staff>
                        </measure>
                    </section>
                </score>
            </mdiv>
        </body>
    </music>
</mei>")

(defn make-mei [v]
  (str mei-header
       (hiccup/hiccup->mei (edn/read-string (str v)))
       mei-footer))

(defonce hiccup-atom
  (r/atom
   [:note {:pname "c"
           :oct   "4"
           :dur   "4"}]))

(defonce file-atom (r/atom (make-mei @hiccup-atom)))

(defn svg-out []
  [:div.svg {:dangerouslySetInnerHTML {:__html (.renderData js/vrvToolkit @file-atom)}}])

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

(defn hiccup-input []
  (let [text (r/atom [:note {:pname "c"
                             :oct   "4"
                             :dur   "4"}])]
    (fn []
      [:div
       [:h3 "Enter MEI in hiccup:"]
       [:textarea
        {:rows      10
         :cols      30
         :value     (str @text)
         :on-change #(do (reset! text (-> % .-target .-value))
                         (when (s/valid? ::hiccup/element @hiccup-atom)
                           (reset! file-atom (make-mei (-> % .-target .-value)))))}]])))

(defn mei-out []
  [:div
   [:h3 "XML:"]
   [:textarea
    {:rows      15
     :cols      50
     :value     @file-atom
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

(comment
@file-atom
(make-mei @hiccup-atom)

  (sequence children [(js->clj (.parseString xml @file-atom) :keywordize-keys true)])
  (sequence (tag= "measure") [(js->clj (.parseString xml @file-atom) :keywordize-keys true)])

  (->> [(js->clj (.parseString xml @file-atom) :keywordize-keys true)]
       (sequence (tag= "measure"))
       count)

  (->> [(js->clj (.parseString xml @file-atom) :keywordize-keys true)]
       (sequence (comp (tag= :chapter)
                       (attr= :name "Conclusion")))
       count))

(defn mecca []
  [:div
   [:h1 "MECCA MEI"]
   [:p "Music data browser"]
   [file-upload]
   [:p]
   [hiccup-input]
   [svg-out]
   [mei-out]
   [edn-out]])

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (r/render-component [mecca] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(defn ^:dev/after-load start []
  (mount-app-element)
  (js/console.log "start"))

(defn ^:export init []
  (js/console.log "init")
  (start))

(defn ^:dev/before-load stop []
  (js/console.log "stop"))
