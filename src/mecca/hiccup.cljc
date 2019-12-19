(ns mecca.hiccup
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::params (s/map-of keyword? any?))

(s/def ::element
   (s/cat
    :tag keyword?
    :attrs (s/? ::params)
    :body (s/* ::node)))

(s/def ::node
  (s/or
   :element ::element
   :number number?
   :string string?))

(declare stringify-mei)

(defn- stringify-params [params]
  (let [kvs (map #(str (name (first %)) "=\"" (second %) "\"" ) params)]
    (if (seq kvs)
      (str " " (str/join " " kvs))
      nil)))

(defn- stringify-element [data]
  (let [tag (name (:tag data))
        params (:attrs data)
        content (:body data)]
    (if (nil? content)
      (str "<" tag (stringify-params params) "/>")
      (let [children (map stringify-mei content)]
        (str "<" tag (stringify-params params) ">" 
            (str/join "" children)
            "</" tag ">")))))
            
(defn- stringify-mei [mei-ast]
  (let [[type data] mei-ast]
    (case type
      :text (str data)
      :element (stringify-element data))))

(defn hiccup->mei [mei]
  (let [parsed-mei (s/conform ::node mei)]
    (if (= :s/invalid parsed-mei)
      (s/explain-str ::node mei)
      (stringify-mei parsed-mei))))

(comment
  (hiccup->mei
        [:section
         [:measure {:n "1"}
          [:staff {:n "1"}
           [:layer
            [:chord {:dur "1"}
             [:note {:pname "c"
                     :oct   "5"}]
             [:note {:pname "g"
                     :oct   "4"}]
             [:note {:pname "e"
                     :oct   "4"}]]]]
          [:staff {:n "2"}
           [:layer
            [:note {:pname "c"
                    :oct   "3"
                    :dur   "1"}]]]]]))